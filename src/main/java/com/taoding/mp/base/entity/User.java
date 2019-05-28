package com.taoding.mp.base.entity;

import com.taoding.mp.base.model.BaseEntity;
import com.taoding.mp.util.BosUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户
 * @author wuwentan
 * @date 2018/8/9
 */
@Data
@Entity
@Table(name = "user")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Column
    private String username;

    /**
     * 密码
     */
    @Column
    private String password;

    /**
     * 姓名
     */
    @Column
    private String name;

    /**
     * 手机号
     */
    @Column
    private String phone;

    /**
     * 工作单位
     */
    @Column
    private String workUnit;

    /**
     * 职位
     */
    @Column
    private String post;

    /**
     * 头像、照片
     */
    @Column
    private String avatar;

    /**
     * 所属部门id，关联department主键
     */
    @Column
    private String deptId;

    /**
     * 所属部门名称
     */
    @Transient
    private String deptName;

    /**
     * 前端展示使用字段
     */
    @Lob
    @Column(columnDefinition = "text")
    private String deptArray;

    /**
     * 是否为系统管理员：默认为空或Null,值为“Y”是系统管理员
     */
    @Column
    private String isAdmin;

    /**
     * 绑定微信openid
     */
    @Column
    private String openId;

    /**
     * 用户身份标识：district（区领导）、dept（部门领导）、staff（办事员）、company（企业）
     */
    @Column
    private String flag;

    /**
     * 状态类型：1.扶贫、2.党建、3.知识产品、4.智慧秦岭、5.全经连
     */
    @Column
    private Integer statusType;

    /**
     * 获取头像照片Url
     * @return
     */
    public String getAvatarUrl() {
        if (StringUtils.isNotBlank(this.avatar)) {
            return BosUtils.getUrlByBosKey(this.avatar);
        }
        return null;
    }

    /**
     * 角色ids的集合
     */
    @Transient
    private List<String> roleIdList = new ArrayList<>();

    /**
     * 角色名称（展示用）
     */
    @Transient
    private List<String> roleNameList = new ArrayList<>();

    /**
     * 角色code
     */
    @Transient
    private List<String> roleCodeList = new ArrayList<>();

}
