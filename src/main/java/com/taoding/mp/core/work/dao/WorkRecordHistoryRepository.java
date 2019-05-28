package com.taoding.mp.core.work.dao;

import com.taoding.mp.core.work.entity.WorkRecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 申报材料历史库
 */
public interface WorkRecordHistoryRepository extends JpaRepository<WorkRecordHistory, String> {

}
