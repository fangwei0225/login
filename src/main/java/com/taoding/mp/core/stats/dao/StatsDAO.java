package com.taoding.mp.core.stats.dao;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.service.DeptService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.stats.vo.CategoryStatsVO;
import com.taoding.mp.core.stats.vo.ProjectStatsVO;
import com.taoding.mp.core.stats.vo.ProjectSummeryVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwentan
 * @date 2019/4/22
 */
@Repository
public class StatsDAO extends BaseDAO {

    @Autowired
    DeptService deptService;
    @Autowired
    ConfigService configService;

    /**
     * 项目统计数据
     * @return
     */
    public ProjectSummeryVO projectStats(){
        String year = configService.getKeyValue("project_year");

        String corpId = UserSession.getUserSession().getCorpId();
        String flag = UserSession.getUserSession().getFlag();
        String deptId = UserSession.getUserSession().getDeptId();
        List<Object> args = new ArrayList<>();
        args.add(corpId);
        args.add(year);
        StringBuilder sql = new StringBuilder("select id,status,SUBSTRING_INDEX(category,' ',1) category,");
        sql.append("IFNULL(total_investment,0) amount from project_info where corp_id = ? and is_delete = 1 ");
        sql.append("and year = ? and group_id is null ");
        //如果不是区级领导则只能看到所属部门的项目信息
        if (!Constants.USER_FLAG_DISTRICT.equals(flag)){
            //获取用户的所属部门
            String parentId = deptService.getParentId(deptId);
            deptId = "0".equals(parentId) ? deptId : parentId;
            sql.append("and find_in_set (?,responsible_unit) ");
            args.add(deptId);
        }
        List<ProjectStatsVO> list = jdbc.query(sql.toString(), args.toArray(), new BeanPropertyRowMapper<>(ProjectStatsVO.class));
        list = CollectionUtils.isNotEmpty(list) ? list : new ArrayList<>();

        //项目总数
        long totalCount = list.size();
        //前期项目数量
        long totalProCount = list.stream().filter(p -> p.getStatus() == 0).count();
        //在建项目数量
        long totalTodoCount = list.stream().filter(p -> p.getStatus() == 1).count();
        //项目总金额
        double totalAmount = list.stream().mapToDouble(ProjectStatsVO::getAmount).sum();
        //前期项目总金额
        double totalProAmount = list.stream().filter(p -> p.getStatus() == 0).mapToDouble(ProjectStatsVO::getAmount).sum();
        //在建项目总金额
        double totalTodoAmount = list.stream().filter(p -> p.getStatus() == 1).mapToDouble(ProjectStatsVO::getAmount).sum();

        //根据项目类别分组数据
        Map<String, List<ProjectStatsVO>> categoryMap = list.stream().collect(Collectors.groupingBy(ProjectStatsVO::getCategory));

        //项目类别统计结果
        List<CategoryStatsVO> categoryList = new ArrayList<>();
        categoryMap.forEach((String category, List<ProjectStatsVO> items) -> {
            //前期项目数量
            long proCount = items.stream().filter(p -> p.getStatus() == 0).count();
            //在建项目数量
            long todoCount = items.stream().filter(p -> p.getStatus() == 1).count();
            //前期项目金额
            double proAmount = items.stream().filter(p -> p.getStatus() == 0).mapToDouble(ProjectStatsVO::getAmount).sum();
            //在建项目金额
            double todoAmount = items.stream().filter(p -> p.getStatus() == 1).mapToDouble(ProjectStatsVO::getAmount).sum();

            CategoryStatsVO categoryStats = new CategoryStatsVO().setCategory(category).setProCount(proCount)
                    .setTodoCount(todoCount).setProAmount(proAmount).setTodoAmount(todoAmount);
            categoryList.add(categoryStats);
        });
        ProjectSummeryVO projectSummery = new ProjectSummeryVO();
        projectSummery.setYear(year);
        projectSummery.setTotalCount(totalCount).setTotalProCount(totalProCount).setTotalTodoCount(totalTodoCount);
        projectSummery.setTotalAmount(totalAmount).setTotalProAmount(totalProAmount).setTotalTodoAmount(totalTodoAmount);
        projectSummery.setCategoryList(categoryList);
        return projectSummery;
    }

}
