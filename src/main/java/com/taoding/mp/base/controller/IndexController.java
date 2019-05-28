package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.entity.UserDevice;
import com.taoding.mp.base.model.LoginCheck;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.LoginService;
import com.taoding.mp.base.service.SessionService;
import com.taoding.mp.base.service.UserDeviceService;
import com.taoding.mp.base.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 系统首页登录操作类
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    LoginService loginService;
    @Autowired
    UserService userService;
    @Autowired
    SessionService sessionService;
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserDeviceService userDeviceService;

    /**
     * app登录
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/login/{type}")
    public ResponseVO<LoginCheck> login(@RequestBody User user, @PathVariable(required = false) String type) {
        LoginCheck check = loginService.checkLogin(user);
        if (200 == check.getCode()) {
            UserSession session = sessionService.newUserSession(check.getUser(), StringUtils.isNotBlank(type) && "app".equalsIgnoreCase(type));
            check.setSessionId(session.getSessionId());
            userDeviceService.save(new UserDevice().setUserId(check.getUser().getId()).setAlias(user.getId()));
        }
        return new ResponseVO(check);
    }

    /**
     * web登录
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/login")
    public ResponseVO<LoginCheck> login(@RequestBody User user) {
        LoginCheck check = loginService.checkLogin(user);
        if (200 == check.getCode()) {
            UserSession session = sessionService.newUserSession(check.getUser(), false);
            check.setSessionId(session.getSessionId());
        }
        return new ResponseVO(check);
    }

    /**
     * 注销
     *
     * @return
     */
    @PostMapping("/logout")
    public ResponseVO logout(HttpServletRequest request) {
        UserSession userSession = UserSession.getUserSession();
        if (Objects.nonNull(userSession)) {
            jdbc.update(" DELETE FROM user_device WHERE user_id = ? ", userSession.getUserId());
        }
        String sessionId = request.getHeader("sessionId");
        sessionService.removeUserSessionById(sessionId);
        return new ResponseVO("");
    }

    /**
     * 重置某个企业的admin账号
     *
     * @param corpId
     * @return
     */
    @GetMapping("/initAdmin")
    public String initAdmin(@RequestParam String corpId) {
        userService.initAdmin(corpId);
        return corpId + "'s admin is reset! please try to login again.";
    }
}
