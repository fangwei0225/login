package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.RoleMenuRepository;
import com.taoding.mp.base.entity.RoleMenu;
import com.taoding.mp.base.service.RoleMenuService;
import com.taoding.mp.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 角色-菜单 Service
 *
 * @author Leon
 * @version 2018/11/8 9:30
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    /**
     * 保存角色-菜单
     *
     * @param roleMenu
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleMenu save(RoleMenu roleMenu) {
        if (StringUtils.isNoneBlank(new String[]{roleMenu.getMenuId(), roleMenu.getRoleId()})) {
            roleMenu.setId(CommonUtils.getUUID());
            roleMenu.setCreateTime(CommonUtils.getStringDate(new Date()));
            roleMenu = roleMenuRepository.save(roleMenu);
        }
        return roleMenu;
    }

    /**
     * 根据roleId删除角色-菜单数据
     *
     * @param roleId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(String roleId) {
        if (StringUtils.isNotBlank(roleId)) {
            roleMenuRepository.deleteAllByRoleId(roleId);
        }
    }
}
