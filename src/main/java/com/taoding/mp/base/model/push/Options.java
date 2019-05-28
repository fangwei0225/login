package com.taoding.mp.base.model.push;

import lombok.Data;

/**
 * 推送可选项,所有属性均为可选
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
@Data
public class Options {

    /**
     * 纯粹用来作为 API 调用标识
     */
    private Integer sendno;

    /**
     * 离线消息保留时长(秒) 默认 86400,该字段对 iOS 的 Notification 消息无效
     */
    private Integer time_to_live;

    /**
     * 要覆盖的消息 ID
     */
    private Long override_msg_id;

    /**
     * APNs 仅对IOS有效, true为生产环境,false为开发环境
     */
    private Boolean apns_production;

    /**
     * 更新iOS 通知的标识符
     */
    private String apns_collapse_id;

    /**
     * 定速推送时长(分钟)
     */
    private Integer big_push_duration;

}
