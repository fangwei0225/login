package com.taoding.mp.core.datetime.VO;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/17 001709:51
 */
@Data
public class DateAddVO implements Serializable {
    private String dates;
    private Integer year;
}
