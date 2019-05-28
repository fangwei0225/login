package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 用户管理接口类
 *
 * @author wuwentan
 * @date 2018/8/9
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param params
     * @return
     */
    PageVO<User> getPage(Map<String, String> params);

    /**
     * 保存用户信息
     *
     * @param user
     * @return
     */
    User save(User user);

    /**
     * 校验用户名是否重复
     * @param userName
     * @return
     */
    Boolean verifyName(String userName);

    /**
     * 根据id查询用户信息
     *
     * @param id 主键id
     * @return
     */
    User getById(String id);

    /**
     * 根据id删除用户信息
     *
     * @param id 主键id
     */
    void deleteById(String id);

    /**
     * 根据用户名和企业id查询用户信息
     *
     * @param username 用户名
     * @param corpId   企业id
     * @return
     */
    User getByUsername(String username, String corpId);

    /**
     * 根据微信绑定openId查询用户信息
     *
     * @param openId 微信绑定openId
     * @return
     */
    User getByOpenId(String openId);

    /**
     * 初始化企业平台管理员账号
     *
     * @param corpId 企业id
     */
    void initAdmin(String corpId);

    /**
     * 重置密码
     *
     * @param user
     * @return
     */
    User resetPwd(User user);

    /**
     * 修改密码
     *
     * @param params
     * @return
     */
    User modifyPwd(Map<String, String> params);

    /**
     * 禁用公司下管理账户启用或禁用
     *
     * @param company
     * @param corpId
     * @param disable if true,the user will be disabled or else enabled.
     */
    void enableUserByCompanyAndCorpId(String company, String corpId, boolean disable);

    /**
     * 查询所有区级领导列表
     * @return
     */
    List<User> listByLeader();

    /**
     * 查询所有企业用户列表
     * @return
     */
    List<User> listByCompany();
}
