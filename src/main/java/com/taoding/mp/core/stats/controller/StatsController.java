package com.taoding.mp.core.stats.controller;

import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.core.stats.service.StatsService;
import com.taoding.mp.core.stats.vo.ProjectSummeryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计
 * @author wuwentan
 * @date 2019/4/22
 */
@RestController
@RequestMapping("/server/stats")
public class StatsController {

    @Autowired
    StatsService statsService;

    /**
     * 项目统计数据
     * @return
     */
    @PostMapping("/project")
    public ResponseVO<ProjectSummeryVO> project() {
        ProjectSummeryVO resultMap = statsService.projectStats();
        return new ResponseVO<>(resultMap);
    }
}
