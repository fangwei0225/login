package com.taoding.mp.core.project.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.service.DeptService;
import com.taoding.mp.base.service.UserService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.project.entity.ProjectYearPlan;
import com.taoding.mp.core.project.service.ProjectInfoService;
import com.taoding.mp.core.project.service.ProjectYearPlanService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.IdWorker;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目信息管理接口类
 * @author wuwentan
 * @date 2019/4/15
 */
@Service
public class ProjectInfoServiceImpl extends BaseDAO implements ProjectInfoService {

    @Autowired
    ProjectInfoRepository projectInfoRepository;
    @Autowired
    ProjectYearPlanService projectYearPlanService;
    @Autowired
    DeptService deptService;
    @Autowired
    UserService userService;
    @Autowired
    ConfigService configService;

    @Override
    public PageVO<ProjectInfo> getPage(Map<String, String> params) {
        //项目年度
        String year = configService.getKeyValue("project_year");

        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String status = params.get("status");
        String type = params.get("type");
        String name = params.get("name");
        String code = params.get("code");
        String category = params.get("category");
        String responsibleUnit = params.get("responsibleUnit");
        String corpId = UserSession.getUserSession().getCorpId();
        String flag = UserSession.getUserSession().getFlag();
        String deptId = UserSession.getUserSession().getDeptId();

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from project_info where corp_id = ? and is_delete = 1 and year = ? ");
        args.add(corpId);
        args.add(year);
        if (StringUtils.isNotBlank(status)) {
            sql.append("and status = ? ");
            args.add(status);
        }
        if (StringUtils.isNotBlank(type)) {
            sql.append("and type = ? ");
            args.add(type);
        }
        if (StringUtils.isNotBlank(name)) {
            sql.append("and name like ? ");
            args.add("%" + name + "%");
        }
        if (StringUtils.isNotBlank(code)){
            sql.append("and code like ? ");
            args.add("%" + code + "%");
        }
        if (StringUtils.isNotBlank(category)) {
            sql.append("and category like ? ");
            args.add("%" + category + "%");
        }
        if (StringUtils.isNotBlank(responsibleUnit)) {
            sql.append("and find_in_set (?,responsible_unit) ");
            args.add(responsibleUnit);
        }
        //如果不是区级领导则只能看到所属部门的项目信息
        if (!Constants.USER_FLAG_DISTRICT.equals(flag)){
            //获取用户的所属部门
            String parentId = deptService.getParentId(deptId);
            deptId = "0".equals(parentId) ? deptId : parentId;
            sql.append("and find_in_set (?,responsible_unit) ");
            args.add(deptId);
        }
        sql.append("and group_id is null order by create_time desc ");
        PageVO<ProjectInfo> page = getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(ProjectInfo.class));
        List<ProjectInfo> list = page.getItems();
        list.forEach(project->{
            //责任单位ids查询单位名称
            String deptName = deptService.getDeptNameByIds(project.getResponsibleUnit());
            project.setResponsibleUnitName(deptName);
        });
        page.setItems(list);
        return page;
    }

    @Override
    public ProjectInfo save(ProjectInfo projectInfo) {
        if(StringUtils.isBlank(projectInfo.getId())){
            projectInfo.setId(IdWorker.createId());
            projectInfo.setCreateTime(CommonUtils.getStringDate(new Date()));
            projectInfo.setCorpId(UserSession.getUserSession().getCorpId());
            projectInfo.setUpdateTime(CommonUtils.getStringDate(new Date()));
            projectInfo.setIsDelete(Constants.STATUE_NORMAL);
            projectInfo.setResult(Constants.PROJECT_RESULT_NONE);
        }else{
            projectInfo.setUpdateTime(CommonUtils.getStringDate(new Date()));
            ProjectInfo source = getById(projectInfo.getId());
            UpdateUtils.copyNonNullProperties(source, projectInfo);
        }
        //获取项目本年度计划信息
        ProjectYearPlan yearPlan = projectInfo.getYearPlan();

        //获取打包项目子项目列表信息
        List<ProjectInfo> childList = projectInfo.getProjectList();
        if(CollectionUtils.isNotEmpty(childList)){
            projectInfo.setIsGroup(1);
        }else{
            projectInfo.setIsGroup(0);
        }
        //保存项目信息
        projectInfo = projectInfoRepository.save(projectInfo);

        //处理打包项目子项目所属打包项目关系
        ProjectInfo finalProjectInfo = projectInfo;
        childList.forEach(project->{
            if(StringUtils.isBlank(project.getId())){
                project.setId(IdWorker.createId());
            }
            project.setIsGroup(0);
            project.setGroupId(finalProjectInfo.getId());
            UpdateUtils.copyNonNullProperties(finalProjectInfo, project);
        });
        projectInfoRepository.saveAll(childList);

        //保存项目本年度计划信息
        if(yearPlan != null){
            if(CollectionUtils.isNotEmpty(yearPlan.getSourceAmount())){
                yearPlan.setMoneySourceAmount(JSONObject.toJSONString(yearPlan.getSourceAmount()));
            }
            if(StringUtils.isBlank(yearPlan.getId())){
                yearPlan.setId(IdWorker.createId());
                yearPlan.setProjectId(projectInfo.getId());
                yearPlan.setYear(projectInfo.getYear());
                yearPlan.setCorpId(projectInfo.getCorpId());
                yearPlan.setCreateTime(CommonUtils.getStringDate(new Date()));
                yearPlan.setUpdateTime(CommonUtils.getStringDate(new Date()));
                yearPlan.setIsDelete(Constants.STATUE_NORMAL);
            }else{
                ProjectYearPlan source = projectYearPlanService.getById(yearPlan.getId());
                UpdateUtils.copyNonNullProperties(source, projectInfo);
            }
            yearPlan = projectYearPlanService.save(yearPlan);
            projectInfo.setYearPlan(yearPlan);
        }
        return projectInfo;
    }

    @Override
    public ProjectInfo checkProjectByCode(String code) {
        String corpId = UserSession.getUserSession().getCorpId();
        List<ProjectInfo> list = projectInfoRepository.findByCodeAndCorpId(code, corpId);
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return new ProjectInfo();
    }

    @Override
    public ProjectInfo getById(String id) {
        ProjectInfo project = projectInfoRepository.findById(id).orElse(new ProjectInfo());

        //获取责任单位-名称，多个用“,”隔开
        String unitName = deptService.getDeptNameByIds(project.getResponsibleUnit());
        project.setResponsibleUnitName(unitName);

        //获取责任科室名称，多个用“,”隔开
        String deptName = StringUtils.isNotBlank(project.getResponsibleDept()) ? getDeptNames(project.getResponsibleDept()) : "";
        project.setResponsibleDeptName(deptName);

        //获取项目所在街办（园区）-名称
        String streetOfficeName = StringUtils.isNotBlank(project.getStreetOffice()) ? getDeptNames(project.getStreetOffice()) : "";
        project.setStreetOfficeName(streetOfficeName);

        //查询项目本年度计划信息
        ProjectYearPlan yearPlan = projectYearPlanService.getByProjectId(id);
        project.setYearPlan(yearPlan);

        //查询打包项目的子项目列表
        List<ProjectInfo> projectList = getByGroupId(id);
        project.setProjectList(projectList);
        return project;
    }

    /**
     * 根据部门科室ids获取名称
     * @param deptIds
     * @return
     */
    private String getDeptNames(String deptIds){
        String str = "";
        List<String> streetOfficeList = CommonUtils.StringToList(deptIds,",");
        for (String streetOfficeId : streetOfficeList){
            str += deptService.getDeptNames(streetOfficeId) + ",";
        }
        str = str.length() > 0 ? str.substring(0,str.lastIndexOf(",")) : "";
        return str;
    }

    @Override
    public void deleteById(String id) {
        String sql = "update project_info set is_delete = ? where id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);

        sql = "update project_year_plan set is_delete = ? where project_id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);
    }

    @Override
    public PageVO<ProjectInfo> leaderPage(Map<String, String> params) {
        String year = configService.getKeyValue("project_year");
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String name = params.get("name");
        String corpId = UserSession.getUserSession().getCorpId();
        String userId = UserSession.getUserSession().getUserId();

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select id,`name`,`status`,type,result,is_group,type_name,");
        sql.append("SUBSTRING_INDEX(category,' ',1) category from project_info where corp_id = ? ");
        sql.append("and is_delete = 1 and year = ? ");
        args.add(corpId);
        args.add(year);
        if (StringUtils.isNotBlank(name)) {
            sql.append("and (name like ? or code like ?) ");
            args.add("%" + name + "%");
            args.add("%" + name + "%");
        }
        if (StringUtils.isNotBlank(userId)) {
            sql.append("and leader = ? ");
            args.add(userId);
        }
        sql.append("and group_id is null order by create_time desc ");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(ProjectInfo.class));
    }

    @Override
    public Map<String, Object> countByStatus() {
        String corpId = UserSession.getUserSession().getCorpId();
        String year = configService.getKeyValue("project_year");

        String sql = "select ifnull(sum(case when status = 0 then 1 else 0 end),0) 'proNum'," +
                "ifnull(sum(case when status = 1 then 1 else 0 end),0) 'todoNum' from project_info " +
                "where corp_id = ? and is_delete = 1 and year = ? and group_id is null order by create_time desc ";
        return jdbc.queryForMap(sql,corpId,year);
    }

    @Override
    public boolean updateResult(String projectId, int result) {
        String sql = "update project_info set result = ? where id = ? ";
        int n = jdbc.update(sql,result,projectId);
        return n > 0;
    }

    @Override
    public PageVO<ProjectInfo> companyPage(Map<String, String> params) {
        String year = configService.getKeyValue("project_year");
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String name = params.get("name");
        String corpId = UserSession.getUserSession().getCorpId();
        String userId = UserSession.getUserSession().getUserId();

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select id,`name`,`status`,type,result,is_group,type_name,");
        sql.append("SUBSTRING_INDEX(category,' ',1) category from project_info where corp_id = ? ");
        sql.append("and is_delete = 1 and year = ? and company_user_id = ? ");
        args.add(corpId);
        args.add(year);
        args.add(userId);
        if (StringUtils.isNotBlank(name)) {
            sql.append("and (name like ? or code like ?) ");
            args.add("%" + name + "%");
            args.add("%" + name + "%");
        }
        sql.append("and group_id is null order by create_time desc ");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(ProjectInfo.class));
    }

    private List<ProjectInfo> getByGroupId(String groupId){
        String year = configService.getKeyValue("project_year");
        String corpId = UserSession.getUserSession().getCorpId();
        String sql = "select *,(select IFNULL(REPLACE(GROUP_CONCAT(`name`),',','-'),'') 'streetOfficeName' from department ";
        sql += "where FIND_IN_SET(id,(select path_ids from department where id = street_office))) 'streetOfficeName' from project_info ";
        sql += "where corp_id = ? and is_delete = 1 and year = ? and group_id = ?";
        return jdbc.query(sql,new BeanPropertyRowMapper<>(ProjectInfo.class),corpId,year,groupId);
    }
}
