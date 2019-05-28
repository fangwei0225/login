package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.model.ResponseVO;

import java.util.List;

/**
 * 角色 Service
 *
 * @author Leon
 * @version 2018/11/6 14:46
 */
public interface RoleService {

    /**
     * 保存
     *
     * @param role
     * @return
     */
    Role save(Role role);

    /**
     * 更新
     *
     * @param role
     * @return
     */
    Role update(Role role);

    /**
     * 删除
     *
     * @param id
     */
    ResponseVO deleteById(String id);

    /**
     * 查询一个
     *
     * @param id
     * @return
     */
    Role findById(String id);


    /**
     * 查询所有
     *
     * @return
     */
    List<Role> findByList();
}
