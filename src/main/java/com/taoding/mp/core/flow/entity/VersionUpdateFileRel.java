package com.taoding.mp.core.flow.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liuxinghong
 * @Description: 新版本更新时，审批流程节点-申报材料id主键关系表
 * @date 2019/4/19 001914:03
 */
@Data
@Entity
@Table(name = "version_update_file_rel")
public class VersionUpdateFileRel extends BaseEntity {
    /**
     * 旧版本主键fileId
     */
    private String newFileId;
    /**
     * 新版本主键fileId
     */
    private String oldFileId;
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
