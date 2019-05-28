package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.RoleMenuRepository;
import com.taoding.mp.base.dao.RoleRepository;
import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.entity.RoleMenu;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.RoleService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 角色 Service
 *
 * @author Leon
 * @version 2018/11/6 14:47
 */
@Service
public class RoleServiceImpl implements RoleService {


    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * 保存
     *
     * @param role
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role save(Role role) {
        role.setId(CommonUtils.getUUID());
        role.setCreateTime(CommonUtils.getStringDate(new Date()));
        List<String> menuIdList = role.getMenuIdList();
        Role rToRe = roleRepository.save(role);
        saveRoleUser(menuIdList, rToRe.getId());
        return rToRe;
    }

    /**
     * 保存角色-用户
     *
     * @param menuIdList
     * @param roleId
     */
    private void saveRoleUser(List<String> menuIdList, String roleId) {
        roleMenuRepository.deleteAllByRoleId(roleId);
        menuIdList.forEach(m -> {
            RoleMenu rm = new RoleMenu();
            rm.setId(CommonUtils.getUUID());
            rm.setCreateTime(CommonUtils.getStringDate(new Date()));
            rm.setMenuId(m);
            rm.setRoleId(roleId);
            roleMenuRepository.save(rm);
        });
    }

    /**
     * 更新
     *
     * @param role
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(Role role) {
        if (StringUtils.isNotBlank(role.getId())) {
            Role sou = roleRepository.findById(role.getId()).orElse(null);
            UpdateUtils.copyNonNullProperties(sou, role);
        }
        List<String> menuIdList = role.getMenuIdList();
        Role rToRe = roleRepository.saveAndFlush(role);
        saveRoleUser(menuIdList, rToRe.getId());
        return rToRe;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO deleteById(String id) {
        if (StringUtils.isNotBlank(id)) {
            if (!isUsedByOtherTable(id)) {
                roleRepository.deleteById(id);
                return new ResponseVO(200, "删除成功");
            }
        }
        return new ResponseVO(500, "数据存在关联，无法删除");
    }

    private boolean isUsedByOtherTable(String id) {
        String sql = "SELECT COUNT(*) FROM role_user WHERE role_id = ? ";
        Integer rows = jdbc.queryForObject(sql, new Object[]{id}, int.class);
        return rows > 0;
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @Override
    public Role findById(String id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null && StringUtils.isNotBlank(id)) {
            String sql = " SELECT menu_id from role_menu where role_id = ? ";
            List<String> menuIdList = jdbc.queryForList(sql, new Object[]{id}, String.class);
            role.setMenuIdList(menuIdList);
        }
        return role;
    }

    /**
     * 查询列表
     *
     * @return
     */
    @Override
    public List<Role> findByList() {
        return roleRepository.findAll(Sort.by(Sort.Direction.DESC, "num"));
    }
}
