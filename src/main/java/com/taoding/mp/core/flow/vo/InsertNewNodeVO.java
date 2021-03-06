package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 更新模板时插入节点数据
 * @date 2019/4/19 001911:05
 */
@Data
public class InsertNewNodeVO implements Serializable {

    /**
     * 审批流程模板id
     */
    @NotNull(message = "流程模板id不能为空")
    private String flowModeId;
    /**
     * 节点名称
     */
    @NotNull(message = "审批节点名称不能为空")
    private String name;

    /**
     * 顶级id（level=0（局级）时topId为顶级局级id，level=1（科级）时，topId为直属上级局级id）
     */
    @NotNull(message = "顶级id不能为空，局级为最顶局级的节点id，科级为科级直属局级的节点id")
    private String topId;
    /**
     * 同级level节点的父id（level=1时顶级parentId=0）可能为多个
     */
    @NotNull(message = "上级节点id不能为空")
    private String parentId;

    /**
     * 同级level子节点的ids（顶级parentId=0）可能为多个（多个,号隔开）
     */
    private String childIds;


    /**
     * 节点层级：0大环节(局级，单位)，1子流程（科级）
     */
    @NotNull(message = "节点层级：0大环节(局级，单位)，1子流程（科级)")
    private Integer level;

    /**
     * 审批顺序号（level为前提，同一层级节点下）
     */
    @NotNull(message = "审批序号不能为空")
    private Integer num;

    /**
     * 审批单位（关联department部门级别ids）
     */
    private String unitIds;

    /**
     * 审批单位-科室级别（关联department部门下的科室级别ids）
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
     * 节点备注
     */
    private String remark;
    /**
     * 审批备注
     */
    private String approveRemark;

    /**
     * 申报资料名称（多个逗号隔开）
     */
    private String fileName;

    /**
     * 项目隶属关系：0无、1省级、2市级、3区级
     */
    private Integer grade;
    /**
     * 审批类型：0科室处理、1街办处理（根据项目信息中的街办id进行创建待办事项）
     */

    private Integer type;
}
