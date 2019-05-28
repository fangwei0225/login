package com.taoding.mp.core.work.dao;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.core.work.entity.WorkLine;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-04-22
 * @modifyTime:
 * @description:
 */
@Repository
public class WorkLineHistoryDAO extends BaseDAO {

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

    private void saveList(List<WorkLine> dataList){
        StringBuilder sql = new StringBuilder(3000);
        sql.append("insert into work_line_history (id, create_time, update_time, corp_id, is_delete, ")
                .append("project_id, flow_model_id, flow_tree_id, name, type, num, unit_ids, dept_ids, has_flow_work_file, ")
                .append("remark, operator_name, status, result, groups, tree_top_id ) values ");

        for (WorkLine workLine : dataList){
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
                .append(workLine.getHasFlowWorkFile()).append(",'").append(workLine.getRemark()==null?"":workLine.getRemark()).append("','")
                .append(workLine.getOperatorName()== null ? "" : workLine.getOperatorName()).append("',").append(workLine.getStatus()).append(",")
                .append(workLine.getResult()).append(",'").append(workLine.getGroups()).append("','")
                .append(workLine.getTreeTopId()).append("') ");
        return sql.toString();
    }
}
