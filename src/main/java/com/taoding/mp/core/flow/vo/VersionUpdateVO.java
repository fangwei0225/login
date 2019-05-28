package com.taoding.mp.core.flow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 版本更新入参
 * @date 2019/4/17 001710:55
 */
@Data
public class VersionUpdateVO implements Serializable {

    /**
     * 当前模板版本的id
     */
    @NotNull(message = "版本id不能为空！")
    private String flowModeId;
    /**
     * 新版本号
     */
    @NotNull(message = "新版本号不能为空！")
    private String version;

    private String name;
}
