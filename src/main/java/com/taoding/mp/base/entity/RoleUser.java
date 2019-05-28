package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 角色用户
 *
 * @author wuwentan
 * @date 2018/11/5
 */
@Data
@Entity
@Table(name = "role_user")
public class RoleUser extends BaseEntity {

    /**
     * 角色Id
     */
    @Column
    private String roleId;

    /**
     * 用户Id
     */
    @Column
    private String userId;
}
