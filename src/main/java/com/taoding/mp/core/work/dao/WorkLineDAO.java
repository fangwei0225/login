package com.taoding.mp.core.work.dao;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.vo.BacklogVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: youngsapling
 * @date: 2019-04-28
 * @modifyTime:
 * @description:
 */
@Repository
public class WorkLineDAO extends BaseDAO {
    /**
     * [过滤逻辑删除]
     * 科室查询待办/已办
     *
     * @param params
     * @return
     */
    public PageVO<BacklogVO> getBacklogForKeShi(Map<String, String> params) {
        String pageNo = params.get("pageNo");
        String pageSize = params.get("pageSize");
        String deptId = params.get("deptId");
        // 1待办  2已办
        String status = params.get("status");
        String name = params.get("name");

        pageNo = pageNo == null ? "1" : pageNo;
        pageSize = pageSize == null ? "15" : pageSize;
        StringBuilder sql = new StringBuilder(100);
        List<Object> args = new ArrayList<>();
        sql.append("select t1.id as project_id, t1.name as project_name, SUBSTRING_INDEX(t1.category,' ',1) category, " +
                "t1.type, t1.type_name, t1.status, t1.is_group, t2.flow_tree_id as flow_tree_id, t2.tree_top_id, t2.groups from project_info t1 RIGHT JOIN ")
                .append("(select project_id, groups, update_time, flow_tree_id, tree_top_id from work_line where 1=1 " +
                        "and is_delete = 1 ");
        if (StringUtils.isNotBlank(status)) {
            sql.append("and status = ? ");
            args.add(status);
        }
        if (StringUtils.isNotBlank(deptId)) {
            sql.append("and dept_ids = ? ");
            args.add(deptId);
        }
        //这里是按组的概念来分组的, 因为查询已办的时候不同的组是要都展示出来的
        sql.append("group by groups) t2 on t1.id = t2.project_id where 1=1 and t1.is_delete = 1 ");
        if (StringUtils.isNotBlank(name)) {
            sql.append("and t1.name like ? ");
            args.add("%" + name + "%");
        }
        sql.append(" ORDER BY t2.update_time DESC ");
        return getPage(sql.toString(), Integer.valueOf(pageNo), Integer.valueOf(pageSize), args, new BeanPropertyRowMapper(BacklogVO.class));
    }

    /**
     * [过滤逻辑删除]
     * 部门查询待办/已办
     *
     * @param params
     * @return
     */
    public PageVO<BacklogVO> getBacklogForBuMen(Map<String, String> params) {
        String pageNo = params.get("pageNo");
        String pageSize = params.get("pageSize");
        String unitIds = params.get("unitIds");
        // 1待办  2已办
        String status = params.get("status");
        String name = params.get("name");

        pageNo = pageNo == null ? "1" : pageNo;
        pageSize = pageSize == null ? "15" : pageSize;
        StringBuilder sql = new StringBuilder(100);
        List<Object> args = new ArrayList<>();
        sql.append("select w.project_id as project_id, p.name as project_name, f.name 'work_line_name', ")
                .append("SUBSTRING_INDEX(p.category,' ',1) category, p.type, p.type_name, p.status, p.is_group, w.groups ")
                .append("from project_info p right join ( ")
                .append("select * from (select project_id, update_time, tree_top_id, groups from work_line where 1=1 and is_delete = 1 ");
        if (StringUtils.isNotBlank(status)) {
            sql.append("and status = ? ");
            args.add(status);
        }
        if (StringUtils.isNotBlank(unitIds)) {
            sql.append("and unit_ids = ? ");
            args.add(unitIds);
        }
        sql.append("order by update_time desc ) t1 group by project_id) w on p.id = w.project_id left join flow_tree f ")
                .append("on f.id = w.tree_top_id where p.is_delete = 1 ");
        if (StringUtils.isNotBlank(name)) {
            sql.append("and p.name like ? ");
            args.add("%" + name + "%");
        }
        return getPage(sql.toString(), Integer.valueOf(pageNo), Integer.valueOf(pageSize), args, new BeanPropertyRowMapper(BacklogVO.class));
    }

    public List<WorkLine> findByProjectIdAndGroupsOrderByNum(String projectId, String groups) {
        StringBuilder sql = new StringBuilder("select * from work_line where 1=1 and is_delete = 1 ");
        List<Object> args = new ArrayList<>();
        if (StringUtils.isNotBlank(projectId)) {
            sql.append("and project_id = ? ");
            args.add(projectId);
        }
        if (StringUtils.isNotBlank(groups)) {
            sql.append("and groups = ? ");
            args.add(groups);
        }
        sql.append("order by num asc ");
        return jdbc.query(sql.toString(), args.toArray(), new BeanPropertyRowMapper<>(WorkLine.class));
    }

    /**
     * 普通的
     */
    public void saveAll(int batch, List<WorkLine> all) {
        if(CollectionUtils.isEmpty(all)){
            return;
        }
        List<WorkLine> dataList = new ArrayList<>(batch);

        int i = 0;
        for (WorkLine workLine : all) {
            dataList.add(workLine);
            if (++i % batch == 0 || i == all.size()) {
                saveList(dataList);
                dataList.clear();
            }
        }
    }

    private void saveList(List<WorkLine> dataList) {
        StringBuilder sql = new StringBuilder(300);
        sql.append("insert into work_line (id, create_time, update_time, corp_id, is_delete, ")
                .append("project_id, flow_model_id, flow_tree_id, name, type, num, unit_ids, dept_ids, has_flow_work_file, ")
                .append("remark, operator_name, status, result, groups, tree_top_id ) values ");

        for (WorkLine workLine : dataList) {
            String sqlFromObject = getSqlFromObject(workLine);
            sql.append(sqlFromObject).append(", ");
        }

        sql.delete(sql.lastIndexOf(","), sql.length());
        jdbc.execute(sql.toString());
    }


    private String getSqlFromObject(WorkLine workLine) {
        StringBuilder sql = new StringBuilder(1000);
        sql.append("('").append(workLine.getId()).append("','").append(workLine.getCreateTime()).append("','")
                .append(workLine.getUpdateTime()).append("','").append(workLine.getCorpId()).append("',")
                .append(workLine.getIsDelete()).append(",'").append(workLine.getProjectId()).append("','")
                .append(workLine.getFlowModelId()).append("','").append(workLine.getFlowTreeId()).append("','")
                .append(workLine.getName()).append("',").append(workLine.getType()).append(",")
                .append(workLine.getNum()).append(",'")
                .append(workLine.getUnitIds()).append("','").append(workLine.getDeptIds()).append("',")
                .append(workLine.getHasFlowWorkFile()).append(",'").append(workLine.getRemark() == null ? "" : workLine.getRemark()).append("','")
                .append(workLine.getOperatorName() == null ? "" : workLine.getOperatorName()).append("',").append(workLine.getStatus()).append(",")
                .append(workLine.getResult()).append(",'").append(workLine.getGroups()).append("','")
                .append(workLine.getTreeTopId()).append("') ");
        return sql.toString();
    }

    /**
     * 使用jdbcTemplate实现的批量插入, 需显式开启.
     *
     * @param workLines
     * @param batch
     */
    @Transient
    public void saveListNoUse(List<WorkLine> workLines, int batch) {
        int count = workLines.size();
        int from = 0;
        int to = 0;
        StringBuilder sqlHead = new StringBuilder(300);
        sqlHead.append("insert into work_line_history (id, create_time, update_time, corp_id, is_delete, ")
                .append("project_id, flow_model_id, flow_tree_id, name, type, num, unit_ids, dept_ids, has_flow_work_file, ")
                .append("remark, operator_name, status, result, groups, tree_top_id ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        while (from != count) {
            to += batch;
            if (to >= count) {
                to = count;
            }
            List<WorkLine> temp = workLines.subList(from, to);
            jdbc.batchUpdate(sqlHead.toString(), temp, temp.size(), (ps, workLine) -> {
                int x = 0;
                ps.setString(++x, workLine.getId());
                ps.setString(++x, workLine.getCreateTime());
                ps.setString(++x, workLine.getUpdateTime());
                ps.setString(++x, workLine.getCorpId());
                ps.setInt(++x, workLine.getIsDelete() == null ? 0 : workLine.getIsDelete());
                ps.setString(++x, workLine.getProjectId());
                ps.setString(++x, workLine.getFlowModelId());
                ps.setString(++x, workLine.getFlowTreeId());
                ps.setString(++x, workLine.getName());
                ps.setInt(++x, workLine.getType() == null ? 0 : workLine.getType());
                ps.setInt(++x, workLine.getNum() == null ? 0 : workLine.getNum());
                ps.setString(++x, workLine.getUnitIds());
                ps.setString(++x, workLine.getDeptIds());
                ps.setInt(++x, workLine.getHasFlowWorkFile() == null ? 0 : workLine.getHasFlowWorkFile());
                ps.setString(++x, workLine.getRemark());
                ps.setString(++x, workLine.getOperatorName());
                ps.setInt(++x, workLine.getStatus() == null ? 0 : workLine.getStatus());
                ps.setInt(++x, workLine.getResult() == null ? 0 : workLine.getResult());
                ps.setString(++x, workLine.getGroups());
                ps.setString(++x, workLine.getTreeTopId());
            });
            from = to;
        }
    }

    public boolean deleteByProjectId(String projectId) {
        Assert.notNull(projectId, "projectId不能为null.");
        String sql = "delete from work_line where project_id = " + projectId;
        jdbc.execute(sql);
        return true;
    }

    public List<WorkLine> findByFlowModelId(String oldFlowModelId) {
        Assert.notNull(oldFlowModelId, "flowModelId 不能为null ");
        String sql = "select * from work_line where flow_model_id = ? ";
        List<Object> args = new ArrayList<>();
        args.add(oldFlowModelId);
        List<WorkLine> query = jdbc.query(sql, args.toArray(), new BeanPropertyRowMapper<>(WorkLine.class));
        return query;
    }

    public void deleteByIdIn(String ids) {
        String sql = "delete from work_line where FIND_IN_SET(id,?) ";
        jdbc.update(sql, ids);
    }
}
