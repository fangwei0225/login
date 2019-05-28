package com.taoding.mp.base.model.push;

import java.util.List;

/**
 * 推送的客户端
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
public class Audience {

    /**
     * APP客户端的tag,最多 20 个
     */
    private List<String> tag;

    /**
     * APP端给用户起的设备别名
     */
    private List<String> alias;

    /**
     * 一次推送最多1000个,客户端集成SDK后可获取到该值
     */
    private List<String> registration_id;

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public List<String> getRegistration_id() {
        return registration_id;
    }

    public void setRegistration_id(List<String> registration_id) {
        this.registration_id = registration_id;
    }
}
