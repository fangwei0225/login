package com.taoding.mp.core.work.eventbus.event;

import lombok.Data;

/**
 * @author: youngsapling
 * @date: 2019-05-20
 * @modifyTime:
 * @description: 项目信息有更新的消息事件
 */
@Data
public class ProjectEvent {
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目类型, 在建或前期
     */
    private Integer projectStatus;
    /**
     * 项目的分组
     */
    private String projectGroupId;
    /**
     * 项目是否是打包项目
     */
    private Integer projectIsGroup;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目的责任单位 --> 要推送的人
     */
    private String projectDeptId;
    /**
     * 项目走到的一级环节的name
     */
    private String mainWorkLineName;
}
