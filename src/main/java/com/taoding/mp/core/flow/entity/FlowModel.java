package com.taoding.mp.core.flow.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 审批流程模板
 * @author wuwentan
 * @date 2019/4/12
 */
@Data
@Entity
@Table(name ="flow_model")
public class FlowModel extends BaseEntity {

    /**
     * 流程名称
     */
    private String name;

    /**
     * 版本号
     */
    private String version;

    /**
     * 模板类型：划拨项目、出让项目、其他项目
     */
    private Integer type;

    /**
     * 该版本是否生效 1.生效 ，0 未生效
     */
    private Integer isEffect;

    /**
     * 是否为最新的版本（1,true，2,false）
     */
    private Integer isLatest;

    /**
     * 是否可以创建新版本(true,false)
     */
    @Transient
    private Boolean isCreateNewVersion;
    /**
     * 类型名称
     */
    @Transient
    private String typeName;

}
