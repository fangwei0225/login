package com.taoding.mp.core.flow.dao;

import com.taoding.mp.core.flow.entity.FlowTree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/16 001609:04
 */
public interface FlowTreeRepository  extends JpaRepository<FlowTree,String> {
    /**
     * 查询局级下科级的第一个节点
     * @param modelId
     * @param topId
     * @param isDelete
     * @return
     */
    List<FlowTree> findFlowTreeByFlowModeIdAndTopIdAndParentIdAndIsDelete(String modelId,String topId,String parentId,Integer isDelete);

    /**
     * 子环节级别，根据父id模糊匹配科级流程节点
     * @param pId
     * @param isDelete
     * @return
     */
    List<FlowTree> findFlowTreeByParentIdLikeAndIsDelete(String pId,Integer isDelete);
    List<FlowTree> findByFlowModeIdAndParentIdLikeAndIsDelete(String flowId,String pId,Integer isDelete);

    /**
     * 查询局级下科级的第一个节点
     * @param topId
     * @param isDelete
     * @return
     */
    List<FlowTree> findFlowTreeByTopIdAndParentIdAndIsDelete(String topId,String parentId,Integer isDelete);

    /**
     * 根据topId查询所有的节点
     * @param top
     * @param isDel
     * @return
     */
    List<FlowTree> findByTopIdAndIsDelete(String top,Integer isDel);

    List<FlowTree> findByFlowModeIdAndTopIdAndIsDelete(String flowModelId,String top,Integer isDel);

    List<FlowTree> findByFlowModeIdAndIsDelete(String modelId,Integer isDelete);

    List<FlowTree> findByPathIdsLikeAndIsDelete(String flowTreeId,Integer isDelete);

    List<FlowTree> findByFlowModeIdAndLevelAndIsDelete(String modelId,Integer level,Integer isDelete);
    List<FlowTree> findByFlowModeIdAndTopIdAndLevelAndIsDelete(String modelId,String topId,Integer level,Integer isDelete);

    List<FlowTree> findByIdIn(List<String> ids);

    List<FlowTree>  findByFlowModeIdAndNameLikeAndIsDeleteAndLevel(String id,String name ,Integer isDelete,Integer level);

    List<FlowTree>  findByTopIdAndGradeInAndLevelAndIsDelete(String topId,Integer[] grades,Integer level,Integer isDelete);
}
