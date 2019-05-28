package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.UserRepository;
import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.entity.RoleUser;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.service.DeptService;
import com.taoding.mp.base.service.RoleUserService;
import com.taoding.mp.base.service.UserService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwentan
 * @date 2018/8/9
 */
@Service("userService")
public class UserServiceImpl extends BaseDAO implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleUserService roleUserService;

    @Autowired
    private ConfigService configService;

    @Autowired
    DeptService deptService;

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public PageVO<User> getPage(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String content = params.get("content");
        String deptId = params.get("deptId");
        String checkChild = params.get("checkChild");
        String corpId = UserSession.getUserSession().getCorpId();

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select u.id,u.name,u.username,u.phone,d.`name` 'deptName',d.dept_array ");
        sql.append("from user u left join department d on u.dept_id = d.id where u.corp_id = ? and u.is_delete = 1 ");
        sql.append("and (u.is_admin is null or u.is_admin = '') ");
        args.add(corpId);
        if(StringUtils.isNotBlank(checkChild) && StringUtils.isNotBlank(deptId)){
            sql.append("and d.path_ids like ? ");
            args.add("%" + deptId + "%");
        }else if(StringUtils.isNotBlank(deptId)){
            sql.append("and u.dept_id = ? ");
            args.add(deptId);
        }
        if (StringUtils.isNotBlank(content)) {
            sql.append("and (u.name like ? or u.username like ? or u.phone like ?) ");
            args.add("%" + content + "%");
            args.add("%" + content + "%");
            args.add("%" + content + "%");
        }
        sql.append("order by u.create_time desc");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(User.class));
    }

    @Override
    public User save(User user) {
        List<String> roleIdList = user.getRoleIdList();
        if (StringUtils.isBlank(user.getId())) {
            user.setId(CommonUtils.getUUID());
            user.setUsername(user.getUsername());
            String defaultPassword = configService.getKeyValue("default_password");
            String password = StringUtils.isBlank(user.getPassword()) ? defaultPassword : user.getPassword();
            user.setPassword(CommonUtils.md5Encode(password).toLowerCase());
            user.setIsDelete(Constants.STATUE_NORMAL);
            user.setCorpId(UserSession.getUserSession().getCorpId());
            user.setCreateTime(CommonUtils.getStringDate(new Date()));
        } else {
            User source = getById(user.getId());
            UpdateUtils.copyNonNullProperties(source, user);
        }
        user = userRepository.save(user);
        // ~~Do user-role config
        roleUserService.deleteByUserId(user.getId());
        String userId = user.getId();
        roleIdList.forEach(roleId -> {
            RoleUser ru = new RoleUser();
            ru.setRoleId(roleId);
            ru.setUserId(userId);
            ru.setCreateTime(CommonUtils.getStringDate(new Date()));
            roleUserService.save(ru);
        });
        String deptName = deptService.getDeptNames(user.getDeptId());
        user.setDeptName(deptName);
        return user;
    }

    @Override
    public Boolean verifyName(String userName) {
        String corpId = UserSession.getUserSession().getCorpId();
        String sql = "select count(*) from user where corp_id = ? and username = ? ";
        long num = jdbc.queryForObject(sql,Integer.class, corpId, userName);
        return 1 > num;
    }

    @Override
    public User getById(String id) {
        User user = userRepository.findById(id).orElse(null);
        if(user != null){
            String deptName = deptService.getDeptNames(user.getDeptId());
            user.setDeptName(deptName);
            setRoleInformation(user);
        }
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
            user.setRoleIdList(roleList.stream().map(Role::getId).collect(Collectors.toList()));
            user.setRoleNameList(roleList.stream().map(Role::getName).collect(Collectors.toList()));
            user.setRoleCodeList(roleList.stream().map(Role::getCode).collect(Collectors.toList()));
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "update user set is_delete = ? where id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);
    }

    @Override
    public User getByUsername(String username, String corpId) {
        User user = userRepository.findByUsernameAndCorpId(username, corpId);
        setRoleInformation(user);
        return user;
    }

    @Override
    public User getByOpenId(String openId) {
        return userRepository.findByOpenId(openId);
    }

    @Override
    public void initAdmin(String corpId) {
        String sql = "delete from `user` where corp_id = ? and username = 'admin' ";
        jdbc.update(sql, corpId);

        User admin = new User();
        admin.setId(CommonUtils.getUUID());
        admin.setUsername("admin");
        String defaultPassword = configService.getKeyValue("default_password");
        admin.setPassword(CommonUtils.md5Encode(defaultPassword).toLowerCase());
        admin.setName("超级管理员");
        admin.setIsAdmin("Y");
        admin.setIsDelete(Constants.STATUE_NORMAL);
        admin.setCreateTime(CommonUtils.getStringDate(new Date()));
        admin.setCorpId(corpId);
        userRepository.save(admin);
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
        }
        return user;
    }

    /**
     * 禁用公司下管理账户启用或禁用
     * @param company
     * @param corpId
     * @param disable if true,the user will be disabled or else enabled.
     */
    @Override
    public void enableUserByCompanyAndCorpId(String company, String corpId, boolean disable) {
        String sql = " UPDATE user SET status = ? WHERE work_unit = ? AND corp_id = ? ";
        Integer status = 0;
        if (!disable) {
            status = 1;
        }
        jdbc.update(sql, status, company, corpId);
    }

    @Override
    public List<User> listByLeader() {
        String corpId = UserSession.getUserSession().getCorpId();
        String flag = Constants.USER_FLAG_DISTRICT;
        String sql = "select * from user where corp_id = ? and is_delete = 1 and (is_admin is null or is_admin = '') and flag = ? order by create_time desc ";
        return jdbc.query(sql,new BeanPropertyRowMapper<>(User.class), corpId, flag);
    }

    @Override
    public List<User> listByCompany() {
        String corpId = UserSession.getUserSession().getCorpId();
        String flag = Constants.USER_FLAG_COMPANY;
        String sql = "select * from user where corp_id = ? and is_delete = 1 and (is_admin is null or is_admin = '') and flag = ? order by create_time desc ";
        return jdbc.query(sql,new BeanPropertyRowMapper<>(User.class), corpId, flag);
    }
}
