package com.taoding.mp.core.project.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.core.project.dao.ProjectYearPlanRepository;
import com.taoding.mp.core.project.entity.ProjectYearPlan;
import com.taoding.mp.core.project.service.ProjectYearPlanService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目年度计划接口类
 * @author wuwentan
 * @date 2019/4/18
 */
@Service
public class ProjectYearPlanServiceImpl extends BaseDAO implements ProjectYearPlanService {

    @Autowired
    ProjectYearPlanRepository projectYearPlanRepository;

    @Override
    public ProjectYearPlan getById(String id) {
        return projectYearPlanRepository.findById(id).orElse(new ProjectYearPlan());
    }

    @Override
    public ProjectYearPlan getByProjectId(String projectId) {
        String sql = "select * from project_year_plan where project_id = ? order by year desc";
        List<ProjectYearPlan> list = jdbc.query(sql,new BeanPropertyRowMapper<>(ProjectYearPlan.class),projectId);
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : new ProjectYearPlan();
    }

    @Override
    public ProjectYearPlan save(ProjectYearPlan yearPlan) {
        return projectYearPlanRepository.saveAndFlush(yearPlan);
    }
}
