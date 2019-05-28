package com.taoding.mp.core.stats.vo;

import lombok.Data;

/**
 * 项目统计
 * @author wuwentan
 * @date 2019/4/23
 */
@Data
public class ProjectStatsVO {

    /**
     * 项目id
     */
    private String id;

    /**
     * 项目状态：0前期、1在建
     */
    private Integer status;

    /**
     * 项目类别（一级类别名称）
     */
    private String category;

    /**
     * 总投资额
     */
    private Double amount;
}
