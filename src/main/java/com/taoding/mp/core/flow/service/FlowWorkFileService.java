package com.taoding.mp.core.flow.service;

import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.flow.vo.FileUpdateVO;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/16 001613:13
 */
public interface FlowWorkFileService {

    /**
     * 根据流程节点id查询申报材料列表
     * @param flowTreeId
     * @return
     */
    List<FlowWorkFile> selectByFlowTreeId(String flowTreeId,Integer isDel);

    /**
     * 根据流程节点更新申报材料

     * @return
     */
    Boolean update(FileUpdateVO vo);

    /**
     * 根据主流程id查询所有子流程的申报材料列表
     * @param id 主流程节点ID
     * @return
     */
    List<FlowWorkFile> fileListByTopId(String id);
}
