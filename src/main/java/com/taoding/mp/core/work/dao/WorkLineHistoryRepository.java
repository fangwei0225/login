package com.taoding.mp.core.work.dao;

import com.taoding.mp.core.work.entity.WorkLineHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 流水线历史记录表
 */
public interface WorkLineHistoryRepository extends JpaRepository<WorkLineHistory, String> {

}
