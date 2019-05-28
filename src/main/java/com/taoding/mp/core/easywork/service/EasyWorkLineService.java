package com.taoding.mp.core.easywork.service;

import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.work.entity.WorkLine;

/**
 * @author: youngsapling
 * @date: 2019-05-14
 * @modifyTime:
 * @description: plan B
 */
public interface EasyWorkLineService {

    /**
     * 通过层级判断查询出对应的办理进度图
     * @param projectId
     * @param level
     * @param flowTreeId
     * @return
     */
    PreviewVO getByLevel(String projectId, Integer level, String flowTreeId);

    /**
     * 用户点击完成/跳过。
     * @param id            即workLineId
     * @param projectId     即项目Id
     * @param flowTreeId    即模板Id
     * @param result        1 完成 / 2 跳过
     * @param remark
     * @param operatorName
     */
    boolean completeWorkLine(WorkLine workLine);

    /**
     * 判断本子流程自己这个科室是否全部操作,(workLine中有对应的记录即完成.)
     *      若是则再判断是否别的科室的也完成,
     *             若是则 添加下一个主节点, 并且更新待办中间表.
     *             若不是则无变化.
     *             均返回true;
     *      若不是则返回false
     *
     * @param projectId 项目id
     * @param flowTreeId 一级节点的id
     */
    boolean toNext(String projectId, String flowTreeId);

    /**
     * 初始化项目流水线.
     * @param projectId
     * @return
     */
    boolean initWorkLine(String projectId);
}
