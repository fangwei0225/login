/**
 * Copyright © 2019, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 消息
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
@Accessors(chain = true)
@Data
@Entity
@Table(name = "message")
public class Message extends BaseEntity {

    /**
     * 消息标题
     */
    @Column(length = 500)
    private String title;

    /**
     * 消息内容
     */
    @Column(length = 1000)
    private String msg;

    /**
     * 业务数据
     */
    @Column(columnDefinition = "text")
    private String extra;

    /**
     * 推送状态
     * @see com.taoding.mp.commons.Constants
     */
    @Column
    private Integer status;

    /**
     * 消息类别
     * 消息分类：1,app消息  2，web端消息
     */
    @Column
    private String category;

    /**
     * 业务链接
     */
    @Column
    private String url;

}
