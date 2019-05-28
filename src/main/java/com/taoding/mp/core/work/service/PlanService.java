package com.taoding.mp.core.work.service;

import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.work.vo.PlanVO;

/**
 * @author: youngsapling
 * @date: 2019-04-25
 * @modifyTime:
 * @description: 办理进度接口
 */
public interface PlanService {
    /**
     * 通过层级判断查询出对应的办理进度图
     * @param projectId
     * @param level
     * @param flowTreeId
     * @return
     */
    PreviewVO getByLevel(String projectId, Integer level, String flowTreeId);

    /**
     * 查询指定二级节点的 节点备注/备注list/申报材料list
     * @param flowTreeId
     * @param workLineId
     * @return
     */
    PlanVO get(String flowTreeId, String workLineId);
}
