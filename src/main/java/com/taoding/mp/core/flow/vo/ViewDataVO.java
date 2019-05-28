package com.taoding.mp.core.flow.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/19 001916:47
 */
@Data
public class ViewDataVO implements Serializable {

    private String key;
    private ViewResponDataVO text;
}
