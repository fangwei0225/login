package com.taoding.mp.base.model.push;

import lombok.Data;

/**
 * 极光推送数据模型
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
@Data
public class PushModel {

    /**
     * 推送唯一标志
     */
    private String cid;

    /**
     * 必填,推送到的客户端操作系统类型 [android,ios]
     */
    private String platform="all";

    /**
     * 必填,要推送的客户端
     */
    private Audience audience;

    /**
     * 通知内容体
     */
    private Notification notification;

    /**
     * 自定义消息
     */
    private Message message;

    /**
     * 可选,推送参数
     */
    private Options options;

}
