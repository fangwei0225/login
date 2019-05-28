package com.taoding.mp.base.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门
 * @author Leon
 * @version 2018/11/14 16:51
 */
@Data
@Entity
@Table(name = "department")
public class Department extends BaseEntity {

    /**
     * 部门名称
     */
    @Column
    private String name;

    /**
     * 部门编码（唯一）
     */
    @Column
    private String code;

    /**
     * 父部门节点（没有父节点值为0）
     */
    @Column
    private String parentId;

    /**
     * 节点id路径（根节点id,上级id,...,本节点id）
     */
    @Lob
    @Column(columnDefinition = "text")
    private String pathIds;

    /**
     * 节点名称路径（根节点名称,上级名称,...,本节点名称）
     */
    @Lob
    @Column(columnDefinition = "text")
    private String pathNames;

    /**
     * 排序号
     */
    @Column
    private Integer num;

    /**
     * 级别：0部门，1科室
     */
    @Column
    private Integer level;

    /**
     * 备注
     */
    @Column
    private String remark;

    /**
     * 前端展示使用字段
     */
    @Lob
    @Column(columnDefinition = "text")
    private String deptArray;

    /**
     * 下一级节点集合
     */
    @Transient
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private List<Department> children = new ArrayList<>();
}
