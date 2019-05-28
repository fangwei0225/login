package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.RoleUser;

/**
 * 用户-角色 Service
 *
 * @author Leon
 * @version 2018/11/7 17:46
 */
public interface RoleUserService {

    /**
     * 保存用户-角色
     *
     * @param roleUser
     * @return
     */
    RoleUser save(RoleUser roleUser);

    /**
     * 根据userId删除角色数据
     *
     * @param userId
     */
    void deleteByUserId(String userId);
}
