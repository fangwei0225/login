package com.taoding.mp.core.work.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


/**
 * @author: youngsapling
 * @date: 2019-04-18
 * @modifyTime:
 * @description:
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class BacklogVO {
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 所属一级节点名称
     */
    private String workLineName;
    /**
     * 项目类别（名称）
     */
    private String category;
    /**
     * 项目id
     */
    private String projectId;

    /**
     * 项目类型：1划拨、2出让、3其他 -- 后面又增加了其他几个.
     */
    private Integer type;

    /**
     * 对应的name
     */
    private String typeName;

    /**
     * 关联到的节点表主键.
     */
    private String flowTreeId;

    /**
     * 关联到的节点的头节点.
     */
    private String treeTopId;

    /**
     * 事项对应到的组属性
     */
    private String groups;
    /**
     * 项目状态：0前期、1在建
     */
    private Integer status;
    /**
     * 是否为打包项目：0普通项目、1打包项目
     */
    private Integer isGroup;
}
