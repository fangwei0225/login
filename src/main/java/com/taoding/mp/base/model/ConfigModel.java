package com.taoding.mp.base.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置
 *
 * @author wuwentan
 * @date 2018/8/15
 */
@Data
public class ConfigModel implements Serializable {

    /**
     * 配置参数
     */
    private String key;

    /**
     * 配置参数值
     */
    private String value;

    /**
     * 配置项描述
     */
    private String description;

    public String getDescription() {
        String description = "";
        if ("max_lend_days".equals(this.key)) {
            description = "最大借阅时长（天）";
        }
        if ("max_lend_count".equals(this.key)) {
            description = "最大借阅数量（本）";
        }
        if ("return_book_remind".equals(this.key)) {
            description = "还书消息推送";
        }
        return description;
    }
}
