package com.taoding.mp.core.project.dao;

import com.taoding.mp.core.project.entity.ProjectYearPlan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 项目年度计划
 * @author wuwentan
 * @date 2019/4/18
 */
public interface ProjectYearPlanRepository extends JpaRepository<ProjectYearPlan,String> {
}
