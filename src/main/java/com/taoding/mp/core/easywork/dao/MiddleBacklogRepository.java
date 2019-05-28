package com.taoding.mp.core.easywork.dao;

import com.taoding.mp.core.easywork.entity.MiddleBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-05-14
 * @modifyTime:
 * @description:
 */
@Repository
public interface MiddleBacklogRepository extends JpaRepository<MiddleBacklog, String> {
    /**
     * 查询某项目某一级节点下的所有科室办理记录.
     * @param projectId
     * @param flowTreeId
     * @param isDelete
     * @return
     */
    List<MiddleBacklog> findByProjectIdAndFlowTreeIdAndIsDelete(String projectId, String flowTreeId, Integer isDelete);

    /**
     * 查询指定科室的记录
     * @param projectId
     * @param flowTreeId
     * @param deptId
     * @param isDelete
     * @return
     */
    MiddleBacklog findByProjectIdAndFlowTreeIdAndDeptIdAndIsDelete(String projectId, String flowTreeId, String deptId, Integer isDelete);
}
