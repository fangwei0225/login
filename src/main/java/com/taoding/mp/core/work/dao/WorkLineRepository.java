package com.taoding.mp.core.work.dao;

import com.taoding.mp.core.work.entity.WorkLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 流水线
 */
public interface WorkLineRepository extends JpaRepository<WorkLine, String> {

    Optional<WorkLine> findByIdAndIsDelete(String id, Integer isDelete);
    /**
     * 查询一个项目所有已落库的流水线节点.
     * @param projectId
     * @param isDelete
     * @return
     */
    List<WorkLine> findByProjectIdAndIsDelete(String projectId, Integer isDelete);

    /**
     * 查询projectId, groups下的所有节点.
     * @param projectId
     * @param groups
     * @return
     */
    List<WorkLine> findByProjectIdAndGroupsInAndIsDeleteOrderByNum(String projectId, List<String> groups, Integer isDelete);

    /**
     * 可能是逗号拼接的多个
     * 查询flowTreeId 对应的workLine.
     * @param flowTreeId
     * @return
     */
    List<WorkLine> findByProjectIdAndFlowTreeIdInAndIsDelete(String projectId, List<String> flowTreeId, Integer isDelete);


    /**
     * 查询flowTreeId 对应的workLine.
     * @param flowTreeId
     * @return
     */
    WorkLine findByProjectIdAndFlowTreeIdAndIsDelete(String projectId, String flowTreeId, Integer isDelete);
    /**
     * 查询flowModelId下的所有.
     * @param flowModelId
     * @return
     */
    List<WorkLine> findByFlowModelIdAndIsDelete(String flowModelId, Integer isDelete);

    /**
     * 给版本更新用的
     * 查询flowModelId下的所有.
     * @param flowModelId
     * @return
     */
    List<WorkLine> findByFlowModelId(String flowModelId);

    /**
     * 查询某项目下treeTopId的全部记录.即 子流程退回(重置)的时候使用
     * @param projectId
     * @param treeTopId
     * @param isDelete
     * @return
     */
    List<WorkLine> findByProjectIdAndTreeTopIdAndIsDelete(String projectId, String treeTopId, Integer isDelete);

    /**
     * 真删除
     * @param projectId
     * @return
     */
    boolean deleteByProjectId(String projectId);
}
