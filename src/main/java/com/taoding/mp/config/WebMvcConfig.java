package com.taoding.mp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 初始化拦截器配置
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    WebInterceptor webInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webInterceptor)
                .addPathPatterns("/server/**")
                .excludePathPatterns("/static/**", "/index/**", "/file/**", "/error")
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
    }
}
