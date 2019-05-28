package com.taoding.mp.base.service;


import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.UserSession;

/**
 * 用户登录session服务类
 *
 * @author wuwentan
 * @date 2018/8/17
 */
public interface SessionService {

    /**
     * 判断是否已登录
     *
     * @param sessionId
     * @return
     */
    boolean exists(String sessionId);

    /**
     * 保存UserSession
     *
     * @param user
     * @param app
     * @return
     */
    UserSession newUserSession(User user, boolean app);

    /**
     * 刷新UserSession有效期
     *
     * @param userSession
     */
    void flushUserSession(UserSession userSession);

    /**
     * 通过sessionId获取UserSession对象
     *
     * @param sessionId
     * @return
     */
    UserSession getUserSessionById(String sessionId);

    /**
     * 根据sessionId删除相关UserSession
     *
     * @param sessionId
     */
    void removeUserSessionById(String sessionId);

    /**
     * 根据userId删除相关UserSession
     *
     * @param userId
     */
    void removeUserSessionByUserId(String userId);

    /**
     * 修改密码后将该用户之前的session相关修改.
     * userId 对应的 sessionId 修改.
     * 旧的sessionId 对应的boundValueOps重命名为新的sessionId
     * @param userId
     * @return
     */
    void updateWithModifyPassword(String userId);

    /**
     * 根据用户id更新用户session
     *
     * @param userId
     */
    void updateUserSessionByUserId(String userId);
}
