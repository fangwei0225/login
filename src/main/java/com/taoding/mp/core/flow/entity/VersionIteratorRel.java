package com.taoding.mp.core.flow.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liuxinghong
 * @Description: 模板新旧版本迭代 flowTreeID节点id关联表
 * @date 2019/4/18 001819:32
 */
@Data
@Entity
@Table(name = "version_iterator_rel")
public class VersionIteratorRel extends BaseEntity {

    /**
     * 旧版本flowtreeId
     */
    private String newId;
    /**
     * 新版本flowtreeId
     */
    private String oldId;
    /**
     * 新版本审批流程模板id,
     */
    private String newFlowModeId;
    /**
     * 旧版本审批流程模板id,
     */
    private String oldFlowModeId;
    /**
     * 该版本是否生效 1.生效 ，0 未生效
     */
    private Integer isEffect;

}
