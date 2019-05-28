package com.taoding.mp.core.flow.vo;

import com.taoding.mp.core.flow.entity.FlowTree;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxinghong
 * @Description: 通过节点id查询节点信息及子节点信息
 * @date 2019/4/16 001609:48
 */
@Data
public class FlowTreeNodeVO implements Serializable {

    /**
     * 流程节点id
     */
    private String flowTreeId;
    /**
     * 审批流程模板id,关联FlowModel主键id
     */
    private String flowModeId;
    /**
     * 流程名称
     */
    private String name;
    /**
     * 节点层级：0大环节，1子流程
     */
    private Integer level;
    /**
     * 顶级id（level=0时topId为顶级局级id，level=1时，topId为直属上级局级id）
     */
    private String topId;
    /**
     * 同级level节点的父id（level=1时顶级parentId=0）
     */
    private String parentId;
    /**
     * 审批顺序号
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
     * 是否有申报资料（1.有，2.没有）
     */
    private Integer hasFlowWorkFile;
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
     * 子节点
     */
    private List<FlowTree> children;


}
