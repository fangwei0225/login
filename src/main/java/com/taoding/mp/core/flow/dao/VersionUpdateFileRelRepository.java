package com.taoding.mp.core.flow.dao;

import com.taoding.mp.core.flow.entity.VersionUpdateFileRel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/19 001916:02
 */
public interface VersionUpdateFileRelRepository extends JpaRepository<VersionUpdateFileRel,String> {

    List<VersionUpdateFileRel> findByNewFlowModeIdAndIsEffectAndIsDelete(String modelId,Integer isEffect,Integer isDelete);

    List<VersionUpdateFileRel> findByNewFileIdInAndIsDelete(List<String> ids, Integer isDelete);
}
