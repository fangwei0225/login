package com.taoding.mp.core.project.dao;

import com.taoding.mp.core.project.entity.ProjectInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 项目信息管理
 * @author wuwentan
 * @date 2019/4/15
 */
public interface ProjectInfoRepository extends JpaRepository<ProjectInfo,String> {

    /**
     * 根据项目代码查询项目信息
     * @param code
     * @param corpId
     * @return
     */
    List<ProjectInfo> findByCodeAndCorpId(String code, String corpId);
}
