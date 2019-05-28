package com.taoding.mp.base.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author wuwentan
 * @date 2018-08-19
 */
@Data
public class BosVO {

    private String key;

    private String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thumbnailUrl;

    public BosVO() {

    }
}
