package com.taoding.mp.core.project.service;

import com.taoding.mp.core.project.entity.ProjectYearPlan;

/**
 * 项目年度计划接口类
 * @author wuwentan
 * @date 2019/4/18
 */
public interface ProjectYearPlanService {

    /**
     * 根据id查询项目年度计划
     * @param id
     * @return
     */
    ProjectYearPlan getById(String id);

    /**
     * 根据项目id查询项目年度计划
     * @param projectId
     * @return
     */
    ProjectYearPlan getByProjectId(String projectId);

    /**
     * 新增、修改项目年度计划
     * @param yearPlan
     * @return
     */
    ProjectYearPlan save(ProjectYearPlan yearPlan);
}
