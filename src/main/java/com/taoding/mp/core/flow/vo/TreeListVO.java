package com.taoding.mp.core.flow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/6 000611:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeListVO implements Serializable {
    private String id;
    private String name;
    private  Integer level;
    private String topId;
    private Integer num;
    private List<TreeListVO> children;
}
