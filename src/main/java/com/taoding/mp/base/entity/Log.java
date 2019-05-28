package com.taoding.mp.base.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 操作日志
 *
 * @author Leon
 * @version 2018/12/7 11:35
 */
@Data
@Entity
@Table(name = "log")
public class Log {

    /**
     * 主键id
     */
    @Id
    @Column(unique = true, length = 32)
    private String id;

    /**
     * 关联业务对象Id
     */
    @Column
    private String businessId;

    /**
     * 操作类型（方法名）
     */
    @Column
    private String type;

    /**
     * 操作内容（参数）
     */
    @Lob
    @Column(columnDefinition = "longtext")
    private String content;

    /**
     * 创建时间
     */
    @Column
    private String createTime;

    /**
     * 操作人（用户的登录名）
     */
    @Column
    private String operator;


    /**
     * 操作人名称
     */
    @Column
    private String operatorName;

    /**
     * 企业id
     */
    @Column
    private String corpId;

}
