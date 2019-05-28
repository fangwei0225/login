package com.taoding.mp.base.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 企业平台信息
 *
 * @author wuwentan
 * @date 2018/11/5
 */
@Data
@Entity
@Table(name = "corp")
public class Corp implements Serializable {

    /**
     * 企业标识，唯一
     */
    @Id
    @Column(length = 32)
    private String corpId;

    /**
     * 企业名称
     */
    @Column
    private String name;

    /**
     * 状态：0不可用、1可用
     */
    @Column
    private Integer status;

    /**
     * 创建时间
     */
    @Column
    private String createTime;

    /**
     * 操作人
     */
    @Column
    private String operator;
}
