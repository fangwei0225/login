package com.taoding.mp.core.flow.vo;

import com.taoding.mp.core.flow.entity.FlowTree;
import lombok.Data;

import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 预览图返回数据
 * @date 2019/4/25 002510:46
 */
@Data
public class ViewResponDataVO extends FlowTree implements Serializable {
    /**
     * 部门名称
     */
    private String unitName;
    /**
     * 科室名称
     */
    private String deptName;
}
