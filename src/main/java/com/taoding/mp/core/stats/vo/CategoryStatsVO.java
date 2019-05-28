package com.taoding.mp.core.stats.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 项目类别统计
 * @author wuwentan
 * @date 2019/4/23
 */
@Accessors(chain = true)
@Data
@JsonInclude(value= JsonInclude.Include.NON_EMPTY)
public class CategoryStatsVO {

    /**
     * 项目类别名称
     */
    private String category;

    /**
     * 前期项目数量
     */
    private Long proCount;

    /**
     * 在建项目数量
     */
    private Long todoCount;

    /**
     * 前期项目金额
     */
    private Double proAmount;

    /**
     * 在建项目金额
     */
    private Double todoAmount;
}
