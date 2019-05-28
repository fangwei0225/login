package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.dao.UserRepository;
import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.service.RoleService;
import com.taoding.mp.base.service.SessionService;
import com.taoding.mp.base.service.UserAdminService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员用户接口类
 * @author wuwentan
 * @date 2019/4/11
 */
@Service("userAdminService")
public class UserAdminServiceImpl extends BaseDAO implements UserAdminService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private ConfigService configService;

    @Autowired
    DeptRepository deptRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    SessionService sessionService;

    @Override
    public PageVO<User> getPage(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String username = params.get("username");
        String phone = params.get("phone");
        String workUnit = params.get("workUnit");
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from user where is_admin = 'Y' ");
        if (StringUtils.isNotBlank(username)) {
            sql.append("and username like ? ");
            args.add("%" + username + "%");
        }
        if (StringUtils.isNotBlank(phone)){
            sql.append("and phone like ? ");
            args.add("%" + phone + "%");
        }
        if (StringUtils.isNotBlank(workUnit)){
            sql.append("and work_unit like ? ");
            args.add("%" + workUnit + "%");
        }
        sql.append("order by create_time desc ");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(User.class));
    }

    @Override
    public User save(User user) {
        if (StringUtils.isBlank(user.getId())) {
            user.setId(CommonUtils.getUUID());
            user.setUsername(user.getUsername());
            String defaultPassword = configService.getKeyValue("default_password");
            String password = StringUtils.isBlank(user.getPassword()) ? defaultPassword : user.getPassword();
            user.setPassword(CommonUtils.md5Encode(password).toLowerCase());
            user.setIsDelete(1);
            //是否为系统管理员设置
            user.setIsAdmin("Y");
            user.setCorpId(UserSession.getUserSession().getCorpId());
            user.setCreateTime(CommonUtils.getStringDate(new Date()));
            user = userRepository.save(user);

        }else {
            User getUser = getById(user.getId());
            UpdateUtils.copyNonNullProperties(getUser, user);
            user = userRepository.save(user);
        }
        return user;
    }

    @Override
    public User getById(String id) {
        User user = userRepository.findById(id).orElse(null);
        setRoleInformation(user);
        return user;
    }

    /**
     * 配置角色相关信息
     * @param user
     */
    private void setRoleInformation(User user) {
        if (user != null && StringUtils.isNotBlank(user.getId())) {
            String sql = "select t1.* from role t1 left join role_user t2 on t1.id = t2.role_id left join user t3 on t3.id = t2.user_id where t3.id = ? ";
            List<Role> roleList = jdbc.query(sql, new Object[]{user.getId()}, new BeanPropertyRowMapper<>(Role.class));
            user.setRoleNameList(roleList.stream().map(Role::getName).collect(Collectors.toList()));
            user.setRoleIdList(roleList.stream().map(Role::getId).collect(Collectors.toList()));
            user.setRoleCodeList(roleList.stream().map(Role::getCode).collect(Collectors.toList()));
        }
    }

    @Override
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getByUsername(String username, String corpId) {
        User user = userRepository.findByUsernameAndCorpId(username, corpId);
        setRoleInformation(user);
        return user;
    }

    /**
     * 重置密码
     * @param user
     * @return
     */
    @Override
    public User resetPwd(User user) {
        //重置密码从系统配置中取默认密码
        String defaultPassword = configService.getKeyValue("default_password");
        user.setPassword(CommonUtils.md5Encode(defaultPassword).toLowerCase());
        User souUser = getById(user.getId());
        UpdateUtils.copyNonNullProperties(souUser, user);
        return userRepository.saveAndFlush(user);
    }

    /**
     * 修改密码
     * @param params
     * @return
     */
    @Override
    public User modifyPwd(Map<String, String> params) {
        UserSession userSession = UserSession.getUserSession();
        User user = null;
        if (StringUtils.isNotBlank(userSession.getUserId())) {
            String oldPassword = params.get("oldPassword");
            user = getById(userSession.getUserId());
            if (!user.getPassword().equalsIgnoreCase(CommonUtils.md5Encode(oldPassword))) {
                return null;
            }
            String newPassword = params.get("newPassword");
            user.setPassword(CommonUtils.md5Encode(newPassword).toLowerCase());
            userRepository.saveAndFlush(user);
            //修改密码后将该用户之前的token置失效.
            sessionService.updateWithModifyPassword(user.getId());
        }
        return user;
    }

    /**
     * 查询所有管理员用户列表
     * @return
     */
    @Override
    public List<User> findByList() {
        String sql = "SELECT * FROM user WHERE status = 1 AND is_admin = 'Y' ";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public Boolean verifyName(final String userName) {
        if(StringUtils.isNotBlank(userName)){
            StringBuilder sql = new StringBuilder();
            List<Object> args = new ArrayList<>();
            sql.append("select username from user where 1 = 1 ");
            sql.append("and username = ?");
            args.add(userName);
            boolean empty = jdbc.query(sql.toString(), args.toArray(), new BeanPropertyRowMapper<>(User.class)).isEmpty();
            return empty;
        }else{
            return true;
        }
    }
}
