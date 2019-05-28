package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/15 001515:54
 */
@Data
public class FlowModelUpdateVO {

    @NotNull(message = "主键不能为空！")
    private String id;
    /**
     * 流程名称
     */
    private String name;

    /**
     * 版本号
     */
    private String version;


}
