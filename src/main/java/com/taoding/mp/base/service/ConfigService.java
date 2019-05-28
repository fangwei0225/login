package com.taoding.mp.base.service;

import com.taoding.mp.base.model.ConfigModel;

import java.util.List;

/**
 * Created by wuwentan on 2018/8/14.
 */
public interface ConfigService {

    List<ConfigModel> getList(String app);

    void setConfig(String key, String value);

    ConfigModel getConfig(String key);

    void setKeyValue(String key, String value, long timeout);

    String getKeyValue(String key);

    void deleteConfig(String key);

    /**
     * 获取知识产权自增序列号
     *
     * @return
     */
    String incrPropertyNum();

    /**
     * 获取产权需求自增序列号
     *
     * @return
     */
    String incrNeedNum();
}
