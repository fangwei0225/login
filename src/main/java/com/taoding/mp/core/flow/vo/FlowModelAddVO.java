package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/15 001514:09
 */
@Data
public class FlowModelAddVO implements Serializable {

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空！")
    private String name;

    /**
     * 版本号
     */
    @NotNull(message = "版本号不能为空！")
    private String version;

    /**
     * 模板类型：划拨项目、出让项目、其他项目
     */
    @NotNull(message = "模板类型(划拨项目、出让项目、其他项目)不能为空！")
    private Integer type;
}
