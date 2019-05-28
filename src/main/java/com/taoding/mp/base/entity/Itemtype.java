package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 登陆用户类型
 *
 * @author fangwei
 * @date 2019/5/27
 */
@Data
@Entity
@Table(name = "itemtype")
public class Itemtype extends BaseEntity{

    /**
     * 用户Id
     */
    @Column
    private String userId;
    /**
     * 用户登陆类型
     */
    @Column
    private String userType;
}
