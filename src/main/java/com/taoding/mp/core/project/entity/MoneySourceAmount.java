package com.taoding.mp.core.project.entity;

import lombok.Data;

/**
 * 年度投资金额及来源
 * @author wuwentan
 * @date 2019/4/14
 */
@Data
public class MoneySourceAmount {

    /**
     * 投资来源
     */
    private String source;

    /**
     * 投资金额
     */
    private String amount;
}
