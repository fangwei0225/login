package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.LoginCheck;

/**
 * @author wuwentan
 * @date 2018/8/9
 */
public interface LoginService {

    /**
     * 登录验证
     *
     * @param user
     * @return
     */
    LoginCheck checkLogin(User user);
}
