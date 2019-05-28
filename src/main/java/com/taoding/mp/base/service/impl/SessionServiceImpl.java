package com.taoding.mp.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.SessionService;
import com.taoding.mp.base.service.UserService;
import com.taoding.mp.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author wuwentan
 * @date 2018/8/17
 */
@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${system.session-times}")
    private long sessionTimes;

    @Value("${system.allow-multi-device-online}")
    private boolean allowMultiDeviceOnline;

    @Autowired
    private UserService userService;

    /**
     * 判断是否已登录
     *
     * @param sessionId
     * @return
     */
    @Override
    public boolean exists(String sessionId) {
        return StringUtils.isNotBlank(sessionId) && redisTemplate.hasKey(sessionId);
    }

    /**
     * 保存UserSession
     *
     * @param user
     * @param app
     * @return
     */
    @Override
    public UserSession newUserSession(User user, boolean app) {
        String sessionId = CommonUtils.getUUID();
        UserSession session = buildUserSession(sessionId, user);
        // Expire the previous sessionId
        processDeviceLoginCheck(user, app);
        String redisUserId = user.getId() + (app ? ":app" : ":web");
        flushUserSession(session, redisUserId);
        return session;
    }

    private void processDeviceLoginCheck(User user, boolean app) {
        String redisUserId = allowMultiDeviceOnline ? processMultiDeviceCheck(user, app) : processSingleDeviceCheck(user);
        if (null != redisUserId && redisTemplate.hasKey(redisUserId)) {
            String oldSessionId = redisTemplate.boundValueOps(redisUserId).get();
            redisTemplate.delete(oldSessionId);
            redisTemplate.delete(redisUserId);
        }
    }

    private String processSingleDeviceCheck(User user) {
        removeUserSessionByUserId(user.getId());
        // Find and clear
        return null;
    }

    private String processMultiDeviceCheck(User user, boolean app) {
        // Only find
        String redisUserId = null;
        if (app && redisTemplate.hasKey(user.getId() + ":app")) {
            redisUserId = user.getId() + ":app";
        } else if (!app && redisTemplate.hasKey(user.getId() + ":web")) {
            redisUserId = user.getId() + ":web";
        }
        return redisUserId;
    }

    private void flushUserSession(UserSession session, String redisUserId) {
        redisTemplate.boundValueOps(session.getSessionId()).set(JSON.toJSONString(session), sessionTimes, TimeUnit.MINUTES);
        redisTemplate.boundValueOps(redisUserId).set(session.getSessionId(), sessionTimes, TimeUnit.MINUTES);
    }

    private String getRedisUserId(String userId, String sessionId) {
        Set<String> userIds = redisTemplate.keys(userId + "*");
        for (String id : userIds) {
            String sessionIdToUse = redisTemplate.boundValueOps(id).get();
            if (sessionId.equalsIgnoreCase(sessionIdToUse)) {
                return id;
            }
        }
        return userId;
    }

    /**
     * 刷新UserSession有效期
     *
     * @param userSession
     */
    @Override
    public void flushUserSession(UserSession userSession) {
        String userId = userSession.getUserId();
        String redisUserId = getRedisUserId(userId, userSession.getSessionId());
        flushUserSession(userSession, redisUserId);
    }

    /**
     * 通过sessionId获取UserSession对象
     *
     * @param sessionId
     * @return
     */
    @Override
    public UserSession getUserSessionById(String sessionId) {
        if (StringUtils.isNotBlank(sessionId)) {
            String sessionStr = redisTemplate.boundValueOps(sessionId).get();
            return JSON.parseObject(sessionStr, UserSession.class);
        }
        return null;
    }

    /**
     * 根据userId删除相关UserSession
     *
     * @param sessionId
     */
    @Override
    public void removeUserSessionById(String sessionId) {
        UserSession userSession = this.getUserSessionById(sessionId);
        if(userSession != null){
            String userId = userSession.getUserId();
            String redisUserId = getRedisUserId(userId, sessionId);
            if (redisTemplate.hasKey(redisUserId) && redisTemplate.hasKey(sessionId)) {
                redisTemplate.delete(sessionId);
                redisTemplate.delete(redisUserId);
            }
        }
    }

    private UserSession buildUserSession(String sessionId, User user) {
        if (null == sessionId || null == user) {
            return new UserSession();
        }
        return new UserSession().setSessionId(sessionId)
                .setUserId(user.getId())
                .setCorpId(user.getCorpId())
                .setDeptId(user.getDeptId())
                .setFlag(user.getFlag())
                .setIsAdmin(user.getIsAdmin())
                .setOpenId(user.getOpenId())
                .setRoleCodeList(user.getRoleCodeList())
                .setUsername(user.getUsername())
                .setName(user.getName());
    }

    /**
     * 根据userId删除相关UserSession
     *
     * @param userId
     */
    @Override
    public void removeUserSessionByUserId(String userId) {
        Set<String> keys = redisTemplate.keys(userId + "*");
        if (keys.size() > 0) {
            for (String key : keys) {
                String oldSessionId = redisTemplate.boundValueOps(key).get();
                redisTemplate.delete(oldSessionId);
                redisTemplate.delete(key);
            }
        }
    }

    @Override
    public void updateWithModifyPassword(String userId){
        removeUserSessionByUserId(userId);
    }

    /**
     * 根据用户id更新用户session
     *
     * @param userId
     */
    @Override
    public void updateUserSessionByUserId(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            Set<String> userIds = redisTemplate.keys(userId + "*");
            for (String id : userIds) {
                String oldSessionId = redisTemplate.boundValueOps(id).get();
                User userToUse = userService.getById(userId);
                UserSession newSession = buildUserSession(oldSessionId, userToUse);
                redisTemplate.boundValueOps(oldSessionId).set(JSON.toJSONString(newSession), sessionTimes, TimeUnit.MINUTES);
            }
        }
    }


}