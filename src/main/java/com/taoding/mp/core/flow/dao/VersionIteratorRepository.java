package com.taoding.mp.core.flow.dao;

import com.taoding.mp.core.flow.entity.VersionIteratorRel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/19 001909:58
 */
public interface VersionIteratorRepository extends JpaRepository<VersionIteratorRel,String> {

    /**
     * 根据modelID获取 新旧版本关系数据
     * @param flowModelId
     * @param isEffect
     * @param isDelete
     * @return
     */
    List<VersionIteratorRel> findAllByNewFlowModeIdAndIsEffectAndIsDelete(String flowModelId,Integer isEffect,Integer isDelete);

    List<VersionIteratorRel> findByNewIdAndIsDelete(String flowTreeId,Integer isDelete);

}
