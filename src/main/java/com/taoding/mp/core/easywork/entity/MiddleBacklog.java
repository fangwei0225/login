package com.taoding.mp.core.easywork.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author: youngsapling
 * @date: 2019-05-14
 * @modifyTime:
 * @description: 待办的中间记录表.
 */
@Data
@Entity
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@Table(name = "middle_backlog")
public class MiddleBacklog extends BaseEntity {
    /**
     * 关联的项目Id
     */
    @Column
    private String projectId;

    /**
     * 关联的流程模板节点Id(1级)[版本切换时要变]
     */
    @Column
    private String flowTreeId;

    /**
     * 关联的办理科室Id
     */
    @Column
    private String deptId;

    /**
     * 状态, 1 处理中, 2 已完成.
     */
    @Column
    private Integer status;
}
