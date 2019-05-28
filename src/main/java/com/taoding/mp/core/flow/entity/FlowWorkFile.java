package com.taoding.mp.core.flow.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 审批流程节点-申报材料
 * @author wuwentan
 * @date 2019/4/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="flow_work_file")
public class FlowWorkFile extends BaseEntity {
    /**
     * 节点唯一识别编码
     */
  //  private String code;
    /**
     * 审批流程节点id,关联FlowTree的主键id
     */
    private String flowTreeId;

    /**
     * 申报材料项名称
     */
    private String name;
}
