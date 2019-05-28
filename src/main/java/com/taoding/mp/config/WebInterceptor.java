package com.taoding.mp.config;

import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用拦截器处理
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@Configuration
public class WebInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    SessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String sessionId = request.getHeader("sessionId");
        if (sessionService.exists(sessionId)) {
            UserSession session = sessionService.getUserSessionById(sessionId);
            sessionService.flushUserSession(session);
            //添加到线程变量中去
            UserSession.setUserSession(session);
            return true;
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", 401);
        resultMap.put("message", "用户未登录!");

        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(JSONObject.toJSONString(resultMap));
        writer.close();
        response.flushBuffer();
        return false;
    }

    /**
     * 移除线程变量.
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserSession.remove();
    }
}
