package com.taoding.mp.core.project.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * 项目信息
 * @author wuwentan
 * @date 2019/4/13
 */
@Data
@Entity
@Table(name = "project_info")
public class ProjectInfo extends BaseEntity {

    /**
     * 项目年度
     */
    @Column
    private String year;

    /**
     * 项目状态：0前期、1在建
     */
    @Column
    private Integer status;

    /**
     * 项目类型：1划拨、2出让、3其他等，来源于数据字典配置
     */
    @Column
    private Integer type;

    /**
     * 项目类型名称
     */
    @Column
    private String typeName;

    /**
     * 项目代码
     */
    @Column
    private String code;

    /**
     * 责任单位 逗号, 拼接
     */
    @Lob
    @Column(columnDefinition = "text")
    private String responsibleUnit;

    /**
     * 责任科室 逗号, 拼接
     */
    @Lob
    @Column(columnDefinition = "text")
    private String responsibleDept;

    /**
     * 项目单位
     */
    @Lob
    @Column(columnDefinition = "text")
    private String projectUnit;

    /**
     * 项目名称
     */
    @Column
    private String name;

    /**
     * 项目类别（名称）
     */
    @Lob
    @Column(columnDefinition = "text")
    private String category;

    /**
     * 项目类别ids
     */
    @Lob
    @Column(columnDefinition = "text")
    private String categoryIds;

    /**
     * 建设性质
     */
    @Column
    private String nature;

    /**
     * 建设规模及主要建设内容
     */
    @Lob
    @Column(columnDefinition = "text")
    private String content;

    /**
     * 总用地（亩）
     */
    @Column
    private String totalUseLand;

    /**
     * 新增建设用地
     */
    @Column
    private String newLand;

    /**
     * 建设地址
     */
    @Column
    private String address;

    /**
     * 建设起止年限
     */
    @Column
    private String startEndYear;

    /**
     * 总投资（万元）
     */
    @Column
    private String totalInvestment;

    /**
     * 上年度工作-完成投资（万元)
     */
    @Column
    private String completionInvestment;

    /**
     * 累计投资金额（万元）
     */
    @Column
    private String grandTotalInvestment;

    /**
     * 资金来源
     */
    @Column
    private String moneySource;

    /**
     * 前期费投资(万元)
     */
    @Column
    private String earlyInvestment;

    /**
     * 本年度前期工作内容
     */
    @Lob
    @Column(columnDefinition = "text")
    private String earlyContent;

    /**
     * 区级包抓领导
     */
    @Column
    private String leader;

    /**
     * 区级包抓领导姓名
     */
    @Column
    private String leaderName;

    /**
     * 备注
     */
    @Column
    private String remark;

    /**
     * 项目隶属关系：0无、1省级、2市级、3区级
     */
    @Column
    private Integer grade;

    /**
     * 项目位置
     */
    @Lob
    @Column(columnDefinition = "text")
    private String streetOffice;

    /**
     * 项目审批状态：0未审批、1审批中、2审批结束
     */
    @Column
    private Integer result;

    /**
     * 项目管理员-姓名
     */
    @Column
    private String pmName;

    /**
     * 项目管理员-电话
     */
    @Column
    private String pmPhone;

    /**
     * 企业用户id
     */
    @Column
    private String companyUserId;

    /**
     * 企业用户名称
     */
    @Column
    private String companyName;

    /**
     * 是否为打包项目：0普通项目、1打包项目
     */
    @Column
    private Integer isGroup;

    /**
     * 所属打包项目id
     */
    @Column
    private String groupId;

    /**
     * 打包子项目列表：只有「项目名称」和「项目位置」字段有值
     */
    @Transient
    private List<ProjectInfo> projectList;

    /**
     * 在建项目-年度计划
     */
    @Transient
    private ProjectYearPlan yearPlan;

    /**
     * 责任单位-名称
     */
    @Transient
    private String responsibleUnitName;

    /**
     * 责任科室-名称
     */
    @Transient
    private String responsibleDeptName;

    /**
     * 项目所在街办（园区）-名称
     */
    @Transient
    private String streetOfficeName;

    /**
     * 获取项目状态名称
     * @return
     */
    public String getStatusName() {
        String statusName = "";
        if (this.status != null) {
            switch (this.status) {
                case 0:
                    statusName = "前期项目";
                    break;
                case 1:
                    statusName = "在建项目";
                    break;
                default:
                    break;
            }
        }
        return statusName;
    }
}
