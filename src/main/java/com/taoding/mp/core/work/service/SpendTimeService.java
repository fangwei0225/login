package com.taoding.mp.core.work.service;

import com.taoding.mp.core.work.entity.SpendTime;
import com.taoding.mp.core.work.entity.WorkLine;

import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-05-16
 * @modifyTime:
 * @description:
 */
public interface SpendTimeService {
    /**
     * 传入的[子流程节点]必须数据齐全!!!!!!!
     * 传入一个子流程节点, 判断[子流程节点]对应的主流程节点是否已在耗时记录表中, 若没有则添加, 若已有则跳过.
     * @param childWorkLine
     */
    void addMainWorkLine(WorkLine childWorkLine);

    /**
     * 传入主流程节点, 更新他的信息.
     * 传入的节点必须属性齐全!!!!!!!!
     * @param mainWorkLine
     */
    void update(WorkLine mainWorkLine);

    /**
     *
     * 计算某节点的花费时间.
     * 1. 如果在耗时表中没有记录, 那么要不是还不该到这个节点, 要不是这个节点下的子流程还没有任何一个操作, 不开启计时.
     * <p>
     * 2. 如果有记录
     * 2.1 该节点的办理期限为0, 说明没有期限,
     * 返回的时候就是已办理时间, 精确到天（工作日） 专家评审时间不计入.
     * 2.2 期限不为0, 那么判断其已完成还是处理中, 若处理中使用当前时间, 已完成使用结束时间, 计算这个一级节点的耗时,
     * spendTime = mainUpdateTime - mainCreateTime - (specialChildUpdateTime - specialChildCreateTime);
     * 再判断是否超时.
     *
     * [返回的是当前情况下计算出来的结果, 需不需要保存由调用的方法决定]
     *
     * @param mainWorkLine
     * @return null 说明有地方是异常, 但是没抛异常出去, 让程序直接返回. 空对象说明还没到这个1级节点, 不应该计算.
     */
    SpendTime calculate(WorkLine mainWorkLine);

    /**
     * 计算该项目下节点的时间信息.
     * @param projectId
     * @param isDelete
     * @return
     */
    List<SpendTime> findByProjectIdAndIsDelete(String projectId, Integer isDelete);
}
