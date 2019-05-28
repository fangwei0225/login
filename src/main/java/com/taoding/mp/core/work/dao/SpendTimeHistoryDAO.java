package com.taoding.mp.core.work.dao;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.core.work.entity.SpendTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: youngsapling
 * @date: 2019-05-20
 * @modifyTime:
 * @description:
 */
@Repository
public class SpendTimeHistoryDAO extends BaseDAO {

    public void saveAll(int batch, List<SpendTime> all) {
        if(CollectionUtils.isEmpty(all)){
            return;
        }
        List<SpendTime> dataList = new ArrayList<>(batch);

        int i = 0;
        for (SpendTime spendTime : all) {
            dataList.add(spendTime);
            if (++i % batch == 0 || i == all.size()) {
                saveList(dataList);
                dataList.clear();
            }
        }
    }

    private void saveList(List<SpendTime> dataList) {
        StringBuilder sql = new StringBuilder(300);
        sql.append("insert into spend_time_history (id, create_time, update_time, corp_id, is_delete, ")
                .append("project_id, flow_model_id, work_line_id, spend, spend_limit, overdue) values ");

        for (SpendTime spendTime : dataList) {
            String sqlFromObject = getSqlFromObject(spendTime);
            sql.append(sqlFromObject).append(", ");
        }

        sql.delete(sql.lastIndexOf(","), sql.length());
        jdbc.execute(sql.toString());
    }

    private String getSqlFromObject(SpendTime spendTime) {
        StringBuilder sql = new StringBuilder(1000);
        sql.append("('").append(spendTime.getId()).append("','").append(spendTime.getCreateTime()).append("','")
                .append(StringUtils.isBlank(spendTime.getUpdateTime()) ? "" : spendTime.getUpdateTime()).append("','")
                .append(spendTime.getCorpId()).append("',")
                .append(spendTime.getIsDelete()).append(",'").append(spendTime.getProjectId()).append("','")
                .append(spendTime.getFlowModelId()).append("','").append(spendTime.getWorkLineId()).append("',")
                .append(spendTime.getSpend()).append(",").append(spendTime.getSpendLimit()).append(",")
                .append(spendTime.getOverdue()).append(") ");
        return sql.toString();
    }
}
