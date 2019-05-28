package com.taoding.mp.core.flow.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * 审批流程节点
 * @author wuwentan
 * @date 2019/4/12
 */
@Data
@Entity
@Table(name = "flow_tree", indexes = {@Index(columnList = "flowModeId"),@Index(columnList = "topId"),@Index(columnList = "level")})
public class FlowTree extends BaseEntity {
    /**
     * 节点唯一识别编码
     */
  //  private String code;
    /**
     * 审批流程模板id,关联FlowModel主键id
     */
    private String flowModeId;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 顶级id（level=0时topId为顶级局级id，level=1时，topId为直属上级局级id）
     */
    private String topId;
    /**
     * 同级level节点的父id（level=1时顶级parentId=0）可能为多个
     */
    @Column(columnDefinition="text")
    private String parentId;
    /**
     * 节点id路径（根节点id,上级id,...,本节点id）
     */
    @Column(columnDefinition="text")
    private String pathIds;
    /**
     * 节点层级：0大环节，1子流程
     */
    private Integer level;
    /**
     * 是否有子级（0为没有子级，如果有子级填写子级节点个数）
     */
    private Integer hasChild;
    /**
     * 连接下个节点id
     */
//    private String nextId;
    /**
     * 是否有申报材料(1.有，2没有)
     */
    private Integer hasFlowWorkFile;
    /**
     * 审批顺序号(正序排列)
     */
    private Integer num;
    /**
     * 审批单位（关联department部门级别ids）
     */
    private String unitIds;
    /**
     * 审批单位-科室（关联department部门下的科室级别ids
     */
    private String deptIds;
    /**
     * 办理期限（天）
     */
    private Integer dealTime;
    /**
     * 局外运转时间（天）
     */
    private Integer outTime;

    /**
     * 项目隶属关系：0无、1省级、2市级、3区级,4 市级集中开工项目
     */
    private Integer grade;
    /**
     * 审批类型：0科室处理、1街办处理（根据项目信息中的街办id进行创建待办事项）2.责任单位科室处理
     */
    private Integer type;

    /**
     * 节点备注
     */
    @Column(columnDefinition="text")
    private String remark;
    /**
     * 审批备注
     */
    @Column(columnDefinition="text")
    private String approveRemark;
    /**
     * 下一级节点集合
     */
    @Transient
    private List<FlowTree> children;
}
