package com.taoding.mp.core.work.eventbus.event;

import lombok.Data;

/**
 * @author: youngsapling
 * @date: 2019-05-10
 * @modifyTime:
 * @description: 流程节点有更新的通知.-->待办的
 */
@Data
public class BacklogEvent {
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目的分组
     */
    private Integer projectIsGroup;
    /**
     * 项目在建/前期的标识
     */
    private Integer status;
    /**
     * 节点名称
     */
    private String workLineName;
    /**
     * 待办任务的分组
     */
    private String groups;
    /**
     * 待办任务的科室
     */
    private String backlogDeptId;
    /**
     * 项目走到了哪个一级节点
     */
    private String flowTreeId;

    /**
     * 项目走到的一级环节的name
     */
    private String mainWorkLineName;
}
