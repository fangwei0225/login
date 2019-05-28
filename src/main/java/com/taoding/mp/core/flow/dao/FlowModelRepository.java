package com.taoding.mp.core.flow.dao;

import com.taoding.mp.core.flow.entity.FlowModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/15 001515:49
 */
public interface FlowModelRepository extends JpaRepository<FlowModel, String> {

    List<FlowModel> findByTypeAndIsDelete(Integer type,Integer isDel);

    List<FlowModel> findByTypeAndIsEffectAndIsLatestAndIsDelete(Integer type,Integer isEffect,Integer isResale,Integer isDel);

}
