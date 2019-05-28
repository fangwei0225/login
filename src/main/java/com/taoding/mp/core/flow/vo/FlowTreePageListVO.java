package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 查询list入参
 * @date 2019/4/17 001713:57
 */
@Data
public class FlowTreePageListVO implements Serializable {
    /**
     * 当前模板版本的id
     */
    @NotNull(message = "版本id不能为空！")
    private String flowModeId;

    private String name;

    /**
     * 所对应的level级别，0，局级 ，1科级
     */
    @NotNull(message = "所对应的level级别，0，局级 ，1科级，不能为空！")
    private Integer level;

    @NotNull(message = "页数不能为空！")
    private Integer pageNo;

    @NotNull(message = "每页数量不能为空！")
    private Integer pageSize;
}
