/**
 * Copyright © 2019, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 消息处理记录
 *
 * @author Leon
 * @version 2019/4/17 10:17
 */
@Accessors(chain = true)
@Data
@Entity
@Table(name = "messageReceiver")
public class MessageReceiver extends BaseEntity {

    /**
     * 消息id
     */
    @Column(length = 32)
    private String messageId;

    /**
     * 接收人id
     */
    @Column(length = 32)
    private String userId;

    /**
     * 状态  0 - 未处理；1 - 已处理。
     */
    @Column
    private Integer status;

    /**
     * 处理时间
     */
    @Column
    private String finishDate;

    // ~~~~~~~~~~~~~~~~~~Views~~~~~~~~~~~~~~~~~~

    /**
     * 消息标题
     */
    @Transient
    private String title;

    /**
     * 消息内容
     */
    @Transient
    private String msg;

    /**
     * 业务数据
     */
    @Transient
    private String extra;

    /**
     * 业务数据对象
     */
    @Transient
    private JSONObject extraObject;

    /**
     * 消息类型
     */
    @Transient
    private String category;


    public JSONObject getExtraObject() {
        if (StringUtils.isNotBlank(extra)) {
            return JSON.parseObject(extra);
        }
        return null;
    }
}
