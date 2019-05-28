package com.taoding.mp.core.work.dao;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.core.work.entity.WorkRecord;
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
public class WorkRecordHistoryDAO extends BaseDAO {

    public void saveAll(int batch, List<WorkRecord> all) {
        if(CollectionUtils.isEmpty(all)){
            return;
        }
        List<WorkRecord> dataList = new ArrayList<>(batch);

        int i = 0;
        for (WorkRecord workLine : all) {
            dataList.add(workLine);
            if (++i % batch == 0 || i == all.size()) {
                saveList(dataList);
                dataList.clear();
            }
        }
    }

    private void saveList(List<WorkRecord> dataList){
        StringBuilder sql = new StringBuilder(300);
        sql.append("insert into work_record_history (id, create_time, update_time, corp_id, is_delete, ")
                .append("work_line_id, flow_tree_id, flow_work_file_id, flow_model_id, type, remark, affix, status) values ");

        for (WorkRecord workRecord : dataList){
            String sqlFromObject = getSqlFromObject(workRecord);
            sql.append(sqlFromObject).append(", ");
        }

        sql.delete(sql.lastIndexOf(","), sql.length());
        jdbc.execute(sql.toString());
    }

    private String getSqlFromObject(WorkRecord workRecord) {
        StringBuilder sql = new StringBuilder();
        sql.append("( '").append(workRecord.getId()).append("','").append(workRecord.getCreateTime()).append("','")
                .append(workRecord.getUpdateTime()).append("','").append(workRecord.getCorpId()).append("',")
                .append(workRecord.getIsDelete()).append(",'").append(workRecord.getWorkLineId()).append("','")
                .append(workRecord.getFlowTreeId()).append("','").append(workRecord.getFlowWorkFileId()).append("','")
                .append(workRecord.getFlowModelId()).append("',").append(workRecord.getType()).append(",'")
                .append(workRecord.getRemark()).append("','").append(workRecord.getAffix()).append("',")
                .append(workRecord.getStatus()).append(") ");
        return sql.toString();
    }
}
