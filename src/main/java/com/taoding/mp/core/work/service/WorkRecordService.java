package com.taoding.mp.core.work.service;

import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.vo.FlowWorkFileVO;

import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 申报材料
 */
public interface WorkRecordService {
    /**
     * 对外提供的接口.添加/修改/删除 申报材料
     * type 区分 备注和申报材料
     * @param workRecord
     * @return
     */
    WorkRecord saveWorkRecord(WorkRecord workRecord);

    /**
     * 查询某节点已将上传了的申报材料(页面中的每一行)
     */
    List<FlowWorkFileVO> getFlowWorkFileVOByWorkLineId(String workLineId);

    /**
     * 查询指定流水线节点的备注
     * @param workLineId
     * @return
     */
    List<WorkRecord> findByWorkLineIdAndType(String workLineId, Integer type);

    /**
     * 逻辑删除入参对应的记录.
     * @param workLineIdList
     */
    void deleteWorkRecordFromWorkLineList(List<String> workLineIdList);
 }
