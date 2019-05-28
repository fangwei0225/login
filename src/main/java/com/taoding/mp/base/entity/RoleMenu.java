package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 角色菜单/权限
 *
 * @author wuwentan
 * @date 2018/11/5
 */
@Data
@Entity
@Table(name = "role_menu")
public class RoleMenu extends BaseEntity {

    /**
     * 角色Id
     */
    @Column
    private String roleId;

    /**
     * 菜单/权限Id
     */
    @Column
    private String menuId;
}
