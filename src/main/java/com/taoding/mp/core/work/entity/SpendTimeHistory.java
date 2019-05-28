package com.taoding.mp.core.work.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author: youngsapling
 * @date: 2019-05-16
 * @modifyTime:
 * @description: 节点流转用时
 */
@Data
@Entity
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@Table(name = "spend_time_history")
public class SpendTimeHistory extends BaseEntity {
    /**
     * 项目id，关联ProjectInfo主键id
     */
    @Column
    private String projectId;

    /**
     * 关联workLine主键id
     */
    @Column
    private String workLineId;

    /**
     * 记录匹配的模板Id, 用于在版本更新时批量修改.
     */
    @Column
    private String flowModelId;

    /**
     * 该节点实际花费的时间
     */
    @Column
    private Integer spend;

    /**
     * 该节点限期多少天
     */
    @Column
    private Integer spendLimit;

    /**
     * 标识该节点是否逾期
     * 0 未逾期 / 1 已逾期
     */
    @Column
    private Integer overdue;
}
