package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 角色 Repository
 *
 * @author Leon
 * @version 2018/11/6 14:48
 */
public interface RoleRepository extends JpaRepository<Role, String> {

}
