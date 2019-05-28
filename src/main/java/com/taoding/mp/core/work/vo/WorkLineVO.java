package com.taoding.mp.core.work.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.work.entity.WorkLine;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


/**
 * @author: youngsapling
 * @date: 2019-04-25
 * @modifyTime:
 * @description:
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class WorkLineVO extends WorkLine {
    private String deptName;
    /**
     * 节点上带的备注 1表示有
     */
    private Integer hasFlowTreeRemark;
    /**
     * workRecord中的备注 1表示有
     */
    private Integer hasRemarkList;
    /**
     * workRecord中的申报材料 1表示有
     */
    private Integer hasDataList;

    /**
     * 项目隶属关系：0无、1省级、2市级、3区级
     */
    private Integer grade;

    /**
     * 对应的workLine是否创建. 0 没有创建   1 已经创建
     */
    private Integer existWorkLine;

    /**
     * 标识登录用户是否可以操作该节点. 1 不可以操作  2 可以操作
     */
    private Integer permission;

    /**
     * 花费时间
     */
    private Integer spend;

    /**
     * 是否逾期
     */
    private Integer overdue;

    @Override
    public void converFlowTreeToWorkLine(FlowTree flowTree){
        if(!Strings.isNullOrEmpty(flowTree.getDeptIds())){
            super.setDeptIds(flowTree.getDeptIds());
        }
        if(!Strings.isNullOrEmpty(flowTree.getUnitIds())){
            super.setUnitIds(flowTree.getUnitIds());
        }
        super.setName(flowTree.getName());
        super.setFlowTreeId(flowTree.getId());
        this.hasFlowTreeRemark = StringUtils.isBlank(flowTree.getRemark()) ? 0 : 1;
    }
}
