package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色信息
 *
 * @author wuwentan
 * @date 2018/11/5
 */
@Data
@Entity
@Table(name = "role")
public class Role extends BaseEntity {

    /**
     * 角色标识，唯一
     */
    @Column
    private String code;

    /**
     * 角色名称
     */
    @Column
    private String name;

    /**
     * 角色级别
     */
    @Column
    private Integer level;

    /**
     * 序号
     */
    @Column
    private Integer num;

    /**
     * 菜单id集合
     */
    @Transient
    private List<String> menuIdList = new ArrayList<>();
}
