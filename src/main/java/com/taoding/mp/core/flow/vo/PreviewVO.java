package com.taoding.mp.core.flow.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author liuxinghong
 * @Description:预览图VO
 * @date 2019/4/17 001717:31
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class PreviewVO implements Serializable {
    /**
     * 数据
     */
    private List<ViewDataVO> dataMap;
    /**
     * 方位
     */
    private List<DirectVO> directionList;

    /**
     * 用WorkLine做节点的数据
     */
    private List<Map<String, Object>> workLineList;
}
