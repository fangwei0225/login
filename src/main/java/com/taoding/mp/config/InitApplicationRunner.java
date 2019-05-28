package com.taoding.mp.config;

import com.google.common.eventbus.EventBus;
import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.service.RoleService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.work.eventbus.eventlistener.BacklogEventListener;
import com.taoding.mp.core.work.eventbus.eventlistener.ProjectEventListener;
import com.taoding.mp.util.BosUtils;
import com.taoding.mp.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.LocalDate;
import java.util.Date;

/**
 * 系统初始化启动加载类
 *
 * @author wuwentan
 * @date 2018/8/17
 */
@Component
@Order(value = 1)
public class InitApplicationRunner implements ApplicationRunner {

    @Value("${baidu.bos.accessKey}")
    private String baiduBosAccessKey;

    @Value("${baidu.bos.secretKey}")
    private String baiduBosSecretKey;

    @Value("${baidu.bos.bucketName}")
    private String baiduBosBucketName;

    @Value("${baidu.bos.expireTime}")
    private String baiduBosExpireTime;

    @Value("${system.init-roles}")
    private Boolean initRoles;

    @Value("${system.jpush-ios-prod-env}")
    private boolean IOSProdEnv;

    @Autowired
    private RestTemplateBuilder builder;

    @Bean
    public RestTemplate restTemplate() {
        return builder.build();
    }

    @Autowired
    private ConfigService configService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BacklogEventListener backlogEventListener;
    @Autowired
    private ProjectEventListener projectEventListener;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("系统初始化配置...");

        // 初始化系统变量（for redis）
        initSystemVariables();

        // 加载百度对象存储BOS服务
        BosUtils.run(baiduBosAccessKey, baiduBosSecretKey, baiduBosBucketName, baiduBosExpireTime);

        // 初始化系统角色（仅在系统首次部署时执行一次)
        if (initRoles) {
            initSystemRole();
        }

        System.out.println("系统初始化完成！");
    }

    /**
     * 允许前端跨域请求配置
     *
     * @return
     */
    private CorsConfiguration corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("*");
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 允许任何路径
        source.registerCorsConfiguration("/**", corsConfig());
        return new CorsFilter(source);
    }

    @Bean
    public EventBus eventBus(){
        EventBus eventBus = new EventBus();
        eventBus.register(backlogEventListener);
        eventBus.register(projectEventListener);
        return eventBus;
    }
    /**
     * 初始化系统变量（for redis）
     */
    private void initSystemVariables() {
        //重置默认密码
        String defaultPassword = configService.getConfig("default_password").getValue();
        if (StringUtils.isBlank(defaultPassword)) {
            configService.setConfig("default_password", "zd666666");
        }

        //项目年度
        String projectYear = configService.getConfig("project_year").getValue();
        if (StringUtils.isBlank(projectYear)){
            int year = LocalDate.now().getYear();
            configService.setConfig("project_year", String.valueOf(year));
        }

        //IOS极光推送环境：true正式，false开发
        String iosProduction = configService.getConfig("ios_production").getValue();
        if(StringUtils.isBlank(iosProduction)){
            configService.setConfig("ios_production", IOSProdEnv ? "true" : "false");
        }
    }

    /**
     * 初始化系统角色（仅在系统首次部署时执行一次）
     */
    private void initSystemRole() {
        Role r1 = new Role();
        r1.setName("管理员");
        r1.setCode("administrators");
        r1.setLevel(1);
        r1.setCreateTime(CommonUtils.getStringDate(new Date()));
        roleService.save(r1);
    }
}
