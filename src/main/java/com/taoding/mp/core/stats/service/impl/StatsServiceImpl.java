package com.taoding.mp.core.stats.service.impl;

import com.taoding.mp.core.stats.dao.StatsDAO;
import com.taoding.mp.core.stats.service.StatsService;
import com.taoding.mp.core.stats.vo.ProjectSummeryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wuwentan
 * @date 2019/4/22
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    StatsDAO statsDAO;

    @Override
    public ProjectSummeryVO projectStats() {
        return statsDAO.projectStats();
    }
}
