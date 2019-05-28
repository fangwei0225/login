package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.model.ConfigModel;
import com.taoding.mp.base.service.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author wuwentan
 * @date 2018/8/14
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Value("${spring.redis.app}")
    String app;

    private String getApp() {
        if (StringUtils.isNotBlank(this.app)) {
            if ("/".contains(this.app)) {
                this.app = app.replaceFirst("/", "");
            }
        } else {
            this.app = "defaultApp";
        }
        return this.app;
    }

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public List<ConfigModel> getList(String app) {
        if (StringUtils.isBlank(app)) {
            app = getApp();
        }
        List<ConfigModel> configList = new ArrayList<>();
        if (app.contains("/")) {
            app = app.replaceFirst("/", "");
        }
        Set<String> keys = redisTemplate.keys(app + "*");
        keys.forEach(key -> {
            if (redisTemplate.boundHashOps(key) != null) {
                Set hKeys = redisTemplate.boundHashOps(key).keys();
                hKeys.forEach(hKey -> {
                    Object hValue = redisTemplate.boundHashOps(key).get(hKey);
                    ConfigModel config = new ConfigModel();
                    config.setKey(hKey.toString());
                    config.setValue(hValue != null ? hValue.toString() : "");
                    configList.add(config);
                });
            }
        });
        return configList;
    }

    @Override
    public void setConfig(String key, String value) {
        redisTemplate.boundHashOps(getApp()).put(key, value);
    }

    @Override
    public ConfigModel getConfig(String key) {
        boolean hasKey = redisTemplate.boundHashOps(getApp()).hasKey(key);
        if (hasKey) {
            String value = redisTemplate.boundHashOps(getApp()).get(key).toString();
            ConfigModel configModel = new ConfigModel();
            configModel.setKey(key);
            configModel.setValue(value);
            return configModel;
        }
        return new ConfigModel();
    }

    @Override
    public void setKeyValue(String key, String value, long timeout) {
        //构建一个string类型的数据
        redisTemplate.boundValueOps(key).set(value);

        //timeout > 0 则设置key的有效时间为timeout（分钟）
        if (timeout > 0) {
            //设置key的有效时间
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
        }
    }

    @Override
    public String getKeyValue(String key) {
        if (StringUtils.isNotBlank(key) && redisTemplate.boundHashOps(getApp()).hasKey(key)) {
            return redisTemplate.boundHashOps(getApp()).get(key).toString();
        }
        return null;
    }

    @Override
    public void deleteConfig(String key) {
        if (StringUtils.isNotBlank(key) && redisTemplate.boundHashOps(getApp()).hasKey(key)) {
            redisTemplate.boundHashOps(getApp()).delete(key);
        }
    }

    @Override
    public String incrPropertyNum() {
        String key = "incr_property_num";
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(getApp() + ":" + key, redisTemplate.getConnectionFactory());
        Long incrPropertyNum = entityIdCounter.getAndIncrement();
        if (incrPropertyNum == 0) {
            incrPropertyNum = entityIdCounter.getAndIncrement();
        }
        String str = "C";
        //格式化为6位数字前面补0
        return str + String.format("%06d", incrPropertyNum);
    }

    @Override
    public String incrNeedNum() {
        String key = "incr_need_num";
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(getApp() + ":" + key, redisTemplate.getConnectionFactory());
        Long incrNeedNum = entityIdCounter.getAndIncrement();
        if (incrNeedNum == 0) {
            incrNeedNum = entityIdCounter.getAndIncrement();
        }
        String str = "X";
        //格式化为6位数字前面补0
        return str + String.format("%06d", incrNeedNum);
    }
}
