package com.taoding.mp.base.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限菜单
 *
 * @author wuwentan
 * @date 2018/11/5
 */
@Data
@Entity
@Table(name = "menu")
public class Menu extends BaseEntity {

    /**
     * 菜单标识，唯一
     */
    @Column
    private String code;

    /**
     * 菜单名称
     */
    @Column
    private String name;

    /**
     * url
     */
    @Column
    private String url;

    /**
     * 类型：菜单或按钮 M/B
     */
    @Column
    private String type;

    /**
     * 上级id
     */
    @Column
    private String parentId;

    /**
     * 排序号
     */
    @Column
    private Integer num;

    /**
     * 前台路由路径
     */
    @Column
    private String path;

    /**
     * 菜单图标
     */
    @Column
    private String icon;

    /**
     * 菜单的所有路径（逗号分隔）
     */
    @Column(length = 1000)
    private String allPath;


    /**
     * 子节点集合
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Menu> children = new ArrayList<>();

    /**
     * 父菜单名称
     */
    @Transient
    private String parentName;
}
