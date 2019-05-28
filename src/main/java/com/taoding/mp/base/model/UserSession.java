package com.taoding.mp.base.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户session对象
 *
 * @author wuwentan
 * @date 2018/8/17
 */
@Accessors(chain = true)
@Data
public class UserSession implements Serializable {

    private static ThreadLocal<UserSession> userSessionHolder = new ThreadLocal<>();

    /**
     * sessionId
     */
    private String sessionId;

    /**
     * 用户实体的id
     */
    private String userId;

    /**
     * 用户实体的用户名
     */
    private String username;

    /**
     * 用户的姓名
     */
    private String name;

    /**
     * 用户所属平台标识
     */
    private String corpId;

    /**
     * 用户的角色id
     */
    private String roleId;

    /**
     * 是否管理员标识
     */
    private String isAdmin;

    /**
     * 微信唯一标识id
     */
    private String openId;

    /**
     * 所属部门
     */
    private String deptId;

    /**
     * 用户身份标识：district（区领导）、dept（部门领导）、staff（办事员）
     */
    private String flag;

    /**
     * 用户的角色标识
     */
    private List<String> roleCodeList = new ArrayList<>();

    /**
     * 获取用户session
     *
     * @return
     */
    public static UserSession getUserSession() {
        UserSession userSession = userSessionHolder.get();
        return userSession;
    }

    public static void setUserSession(UserSession userSession){
        userSessionHolder.set(userSession);
    }

    public static void remove(){
        userSessionHolder.remove();
    }
}
