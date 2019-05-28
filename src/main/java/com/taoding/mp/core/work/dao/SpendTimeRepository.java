package com.taoding.mp.core.work.dao;

import com.taoding.mp.core.work.entity.SpendTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author: youngsapling
 * @date: 2019-05-16
 * @modifyTime:
 * @description:
 */
public interface SpendTimeRepository extends JpaRepository<SpendTime, String> {
    /**
     * 通过workLineId查询有无对应的记录, 对于不同的项目来说, 某个节点的workLine的Id是不同的.
     * @param workLineId
     * @param isDelete
     * @return
     */
    Optional<SpendTime> findByWorkLineIdAndIsDelete(String workLineId, Integer isDelete);

    List<SpendTime> findByProjectIdAndIsDelete(String projectId, Integer isDelete);
}
