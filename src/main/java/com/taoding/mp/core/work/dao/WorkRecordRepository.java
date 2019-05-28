package com.taoding.mp.core.work.dao;

import com.taoding.mp.core.work.entity.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 申报材料
 */
public interface WorkRecordRepository extends JpaRepository<WorkRecord, String> {

    /**
     * 通过id 和 删除标识 查询这个节点的记录。只是发生了的。
     * 然后在使用的地方匹配一下
     * @param workLineId
     * @param type 0备注  1申报材料
     * @param isDelete
     * @return
     */
    List<WorkRecord> findByWorkLineIdAndTypeAndIsDeleteOrderByCreateTimeDesc(String workLineId, Integer type, Integer isDelete);

    /**
     * 通过workLineId 和 删除标识 查询这个节点的申报材料记录。只是发生了的。
     * 然后在使用的地方匹配一下
     * @param workLineId
     * @param isDelete
     * @return
     */
    List<WorkRecord> findByWorkLineIdAndIsDelete(String workLineId, Integer isDelete);

    /**
     * 通过workLineId 和 删除标识 查询这个节点的申报材料记录
     * @param workLineIds
     * @param isDelete
     * @return
     */
    List<WorkRecord> findByWorkLineIdInAndIsDelete(List<String> workLineIds, Integer isDelete);

    /**
     *
     * @param flowWorkFileId
     * @return
     */
    List<WorkRecord> findByFlowWorkFileIdAndIsDelete(String flowWorkFileId, Integer isDelete);

    /**
     * 查询flowModelId下的所有.
     * @param flowModelId
     * @return
     */
    List<WorkRecord> findByFlowModelId(String flowModelId);
}
