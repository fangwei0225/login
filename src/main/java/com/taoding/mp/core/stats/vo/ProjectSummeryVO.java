package com.taoding.mp.core.stats.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 项目统计汇总
 * @author wuwentan
 * @date 2019/4/23
 */
@Accessors(chain = true)
@Data
public class ProjectSummeryVO {

    /**
     * 项目年度
     */
    private String year;

    /**
     * 项目总数
     */
    private Long totalCount;

    /**
     * 前期项目数量
     */
    private Long totalProCount;

    /**
     * 在建项目数量
     */
    private Long totalTodoCount;

    /**
     * 项目总金额
     */
    private Double totalAmount;

    /**
     * 前期项目金额
     */
    private Double totalProAmount;

    /**
     * 在建项目金额
     */
    private Double totalTodoAmount;

    /**
     * 项目类别统计结果
     */
    private List<CategoryStatsVO> categoryList;
}
