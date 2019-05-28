package com.taoding.mp.core.flow.dao;

import com.taoding.mp.core.flow.entity.FlowWorkFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/16 001613:14
 */
public interface FlowWorkFileRepository extends JpaRepository<FlowWorkFile,String> {

    List<FlowWorkFile> findAllByFlowTreeIdAndIsDelete(String flowId,Integer isDel);

    List<FlowWorkFile> findAllByFlowTreeIdInAndIsDelete(List<String> ids,Integer isDelete);

}
