package com.taoding.mp.base.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * 实体类基类
 * @author wuwentan
 * @date 2018/8/9
 */
@Data
@MappedSuperclass
public class BaseEntity implements Serializable {

    /**
     * 主键id
     */
    @Id
    @Column(unique = true, length = 32)
    private String id;

    /**
     * 创建时间
     */
    @Column
    private String createTime;

    /**
     * 修改时间
     */
    @Column
    private String updateTime;

    /**
     * 企业id
     */
    @Column
    private String corpId;

    /**
     * 删除状态 0删除/1有效
     */
    @Column
    private Integer isDelete;
}
