package com.taoding.mp.core.work.vo;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.core.work.entity.WorkRecord;
import lombok.Data;

import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-04-26
 * @modifyTime:
 * @description:
 */
@Data
public class PlanVO {
    /**
     * 流程节点说明
     */
    private String flowTreeRemark;
    /**
     * 进度说明
     */
    private List<WorkRecord> remarkList;
    /**
     * 申报材料
     */
    private List<WorkRecord> dataList;
    /**
     * 部门领导
     */
    private User dept;
    /**
     * 办事员
     */
    private User staff;
}
