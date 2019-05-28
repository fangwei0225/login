package com.taoding.mp.core.company.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 企业信息（项目单位）
 * @author wuwentan
 * @date 2019/5/7
 */
@Data
@Entity
@Table(name = "company")
public class Company extends BaseEntity {

    /**
     * 企业名称
     */
    @Column
    private String name;

    /**
     * 企业所在地址
     */
    @Column
    private String address;

    /**
     * 企业负责人账号，关联User对象的id
     */
    @Column
    private String userId;

    /**
     * 企业负责人名称
     */
    @Column
    private String leader;

    /**
     * 企业负责人联系电话
     */
    @Column
    private String phone;
}
