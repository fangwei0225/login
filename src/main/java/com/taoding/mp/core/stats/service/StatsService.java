package com.taoding.mp.core.stats.service;

import com.taoding.mp.core.stats.vo.ProjectSummeryVO;

/**
 * @author wuwentan
 * @date 2019/4/22
 */
public interface StatsService {

    /**
     * 项目统计数据
     * @return
     */
    ProjectSummeryVO projectStats();
}
