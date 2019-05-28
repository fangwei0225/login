package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 修改节点
 * @date 2019/4/16 001618:07
 */
@Data
public class FlowTreeUpdateVO implements Serializable {

    /**
     * 审批流程模板id
     */
    @NotNull(message = "流程id不能为空")
    private String id;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点备注
     */
    private String remark;

    /**
     * 办理期限（天）
     */
    private Integer dealTime;

    /**
     * 局外运转时间（天）
     */
    private Integer outTime;

    /**
     * 审批资料以，分割
     */
    private String  fileName;

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
     * 部门级别名称
     */
    private String unitName;
    /**
     * 科室级别名称
     */
    private String deptIdName;
    /**
     * 项目隶属关系：0无、1省级、2市级、3区级
     */
    private Integer grade;
    /**
     * 审批类型：0科室处理、1街办处理（根据项目信息中的街办id进行创建待办事项）
     */
    private Integer type;

}
