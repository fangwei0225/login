package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.RoleMenu;

/**
 * 角色-菜单 Service
 *
 * @author Leon
 * @version 2018/11/8 9:29
 */
public interface RoleMenuService {

    /**
     * 保存角色-菜单
     *
     * @param roleMenu
     * @return
     */
    RoleMenu save(RoleMenu roleMenu);

    /**
     * 根据roleId删除角色数据
     *
     * @param roleId
     */
    void deleteByRoleId(String roleId);
}
