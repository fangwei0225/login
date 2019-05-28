package com.taoding.mp.core.project.service;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.core.project.entity.ProjectInfo;

import java.util.Map;

/**
 * 项目信息管理接口
 * @author wuwentan
 * @date 2019/4/15
 */
public interface ProjectInfoService {

    /**
     * 分页查询项目列表
     * @param params
     * @return
     */
    PageVO<ProjectInfo> getPage(Map<String, String> params);

    /**
     * 新增、修改项目信息
     * @param projectInfo
     * @return
     */
    ProjectInfo save(ProjectInfo projectInfo);

    /**
     * 校验项目代码是否重复，如果已存在则带出项目信息，不存在返回new ProjectInfo();
     * @param code
     * @return
     */
    ProjectInfo checkProjectByCode(String code);

    /**
     * 根据id查询项目信息
     * @param id
     * @return
     */
    ProjectInfo getById(String id);

    /**
     * 根据id逻辑删除项目信息
     * @param id
     */
    void deleteById(String id);

    /**
     * 领导关注-项目列表分页
     * @param params
     * @return
     */
    PageVO<ProjectInfo> leaderPage(Map<String, String> params);

    /**
     * 根据项目状态统计数量
     * @return
     */
    Map<String,Object> countByStatus();

    /**
     * 更新项目审批状态
     * @param projectId 项目id
     * @param result 状态：0未审批、1审批中、2审批结束
     * @return
     */
    boolean updateResult(String projectId, int result);

    /**
     * 企业用户-我的项目分页列表
     * @param params
     * @return
     */
    PageVO<ProjectInfo> companyPage(Map<String, String> params);

}
