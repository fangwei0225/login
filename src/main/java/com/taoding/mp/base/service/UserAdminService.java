package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户接口类
 * @author wuwentan
 * @date 2019/4/11
 */
public interface UserAdminService {

    /**
     * 分页查询用户列表
     * @param params
     * @return
     */
    PageVO<User> getPage(Map<String, String> params);

    /**
     * 保存用户信息
     * @param user
     * @return
     */
    User save(User user);

    /**
     * 根据id查询用户信息
     * @param id  主键id
     * @return
     */
    User getById(String id);

    /**
     * 根据id删除用户信息
     * @param id  主键id
     */
    void deleteById(String id);

    /**
     * 根据用户名和企业id查询用户信息
     * @param username  用户名
     * @param corpId  企业id
     * @return
     */
    User getByUsername(String username, String corpId);

    /**
     * 重置密码
     * @param user
     * @return
     */
    User resetPwd(User user);

    /**
     * 修改密码
     * @param params
     * @return
     */
    User modifyPwd(Map<String, String> params);

    /**
     * 查询所有管理用户列表
     * @return
     */
    List<User> findByList();

    /**
     * 验证用户名是否重复
     * @param userName
     * @return
     */
    Boolean verifyName(String userName);
}
