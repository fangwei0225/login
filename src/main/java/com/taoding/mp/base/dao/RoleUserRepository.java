package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.RoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户-角色 Repository
 *
 * @author Leon
 * @version 2018/11/7 17:48
 */
public interface RoleUserRepository extends JpaRepository<RoleUser, String> {

    void deleteAllByUserId(String userId);
}
