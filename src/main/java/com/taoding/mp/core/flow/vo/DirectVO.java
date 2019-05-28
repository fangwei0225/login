package com.taoding.mp.core.flow.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 方向
 * @date 2019/4/17 001717:54
 */
@Data
public class DirectVO implements Serializable {
    private String from;
    private String to;
}
