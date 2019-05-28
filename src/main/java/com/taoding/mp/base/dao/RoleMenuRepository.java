package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 角色-菜单 Repository
 *
 * @author Leon
 * @version 2018/11/8 9:37
 */
public interface RoleMenuRepository extends JpaRepository<RoleMenu, String> {

    void deleteAllByRoleId(String roleId);

}
