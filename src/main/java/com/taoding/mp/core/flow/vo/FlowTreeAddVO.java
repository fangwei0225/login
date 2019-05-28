package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 模板节点添加vo
 * @date 2019/4/15 001517:16
 */
@Data
public class FlowTreeAddVO implements Serializable {

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
     * 申报资料名称（多个逗号隔开）
     */
    private String fileName;

}
