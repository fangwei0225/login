package com.taoding.mp.core.work.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import com.taoding.mp.core.flow.entity.FlowTree;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.List;

/**
 * 项目审批流水线
 * @author wuwentan
 * @date 2019/4/13
 */
@Data
@Entity
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@Table(name = "work_line")
public class WorkLine extends BaseEntity {

    /**
     * 项目id，关联ProjectInfo主键id
     */
    @Column
    private String projectId;

    /**
     * 审批流程模板id，关联FlowModel主键id [版本切换时要变]
     */
    @Column
    private String flowModelId;

    /**
     * 审批流程节点id，关联FlowTree主键id [版本切换时要变]
     */
    @Column
    private String flowTreeId;

    /**
     * 节点名称 [版本切换时要变]
     */
    @Column
    private String name;

    /**
     * 节点类型
     */
    @Column
    private Integer type;
    /**
     * 审批顺序号(正序排列)
     */
    @Column
    private Integer num;

    /**
     * 审批单位（关联department部门级别ids）
     */
    @Lob
    @Column(columnDefinition="text")
    private String unitIds;

    /**
     * 审批单位（关联Department主键ids）
     */
    @Lob
    @Column(columnDefinition="text")
    private String deptIds;

    /**
     * 是否有申报材料(1.有，2.没有)    [版本切换时要变]
     */
    @Column
    private Integer hasFlowWorkFile;
    /**
     * 操作时填写的内容.
     */
    @Lob
    @Column(columnDefinition="text")
    private String remark;

    /**
     * 操作人姓名.
     */
    @Column
    private String operatorName;

    /**
     * 处理状态：0待处理, 1处理中, 2已处理
     */
    @Column
    private Integer status;

    /**
     * 处理结果：0未处理, 1已确认，2跳过
     */
    @Column
    private Integer result;

    /**
     * 将多个同一科室同一次确定要做的节点转换为流水线, 设置同一个随机值.
     */
    @Column
    private String groups;

    /**
     * 对应节点的头结点id,是所属的一级节点id   [版本切换时要变]
     */
    @Column
    private String treeTopId;

    /**
     * 备注
     */
    @Transient
    private List<WorkRecord> remarkList;

    public void converFlowTreeToWorkLine(FlowTree flowTree){
        this.name = flowTree.getName();
        this.flowModelId = flowTree.getFlowModeId();
        this.flowTreeId = flowTree.getId();
        this.hasFlowWorkFile = flowTree.getHasFlowWorkFile();
        this.treeTopId = flowTree.getTopId();
        //如果是街办办理, 这两个参数在构造List<FlowTree>的时候, 已经正确注入进去了.
        if(StringUtils.isNotBlank(flowTree.getDeptIds())){
            this.deptIds = flowTree.getDeptIds();
        }
        if(StringUtils.isNotBlank(flowTree.getUnitIds())){
            this.unitIds = flowTree.getUnitIds();
        }
    }
}
