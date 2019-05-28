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
 * 用户 - 设备
 *
 * @author Leon
 * @version 2019/4/17 10:57
 */
@Accessors(chain = true)
@Data
@Entity
@Table(name = "userDevice")
public class UserDevice extends BaseEntity {

    /**
     * 用户id
     */
    @Column(length = 32)
    private String userId;

    /**
     * 用户账户
     */
    @Column
    private String username;

    /**
     * 设备id(未使用)
     */
    @Column(unique = true)
    private String registrationId;

    /**
     * APP客户端的用户标签,多个标签用逗号分隔
     */
    @Column(length = 1000)
    private String tags;

    /**
     * APP客户端的用户的别名,每个用户对应一个别名
     * 使用用户的id作为其标识
     */
    @Column(length = 200)
    private String alias;

    /**
     * 客户端平台(Android,IOS)
     */
    @Column(length = 64)
    private String platform;

}
