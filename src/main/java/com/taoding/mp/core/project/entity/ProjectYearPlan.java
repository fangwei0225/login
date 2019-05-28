package com.taoding.mp.core.project.entity;

import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目年度计划
 * @author wuwentan
 * @date 2019/4/14
 */
@Data
@Entity
@Table(name = "project_year_plan")
public class ProjectYearPlan extends BaseEntity {

    /**
     * 项目id
     */
    @Column
    private String projectId;

    /**
     * 项目年度
     */
    @Column
    private String year;

    /**
     * 年度投资金额及来源
     */
    @Column
    private String moneySourceAmount;

    /**
     * 使用新增建设用地面积（亩）
     */
    @Column
    private String newUseLand;

    /**
     * 计划开（复）工  时间
     */
    @Column
    private String planStartDate;

    /**
     * 主要建设内容及年底形象进度
     */
    @Lob
    @Column(columnDefinition = "text")
    private String planContent;

    /**
     * 投资资金来源
     */
    @Transient
    private List<MoneySourceAmount> sourceAmount;

    /**
     * 通过moneySourceAmount转化为对象集合返回
     * @return
     */
    public List<MoneySourceAmount> getSourceAmount(){
        if(StringUtils.isNotBlank(this.moneySourceAmount)){
            return JSONObject.parseArray(this.moneySourceAmount,MoneySourceAmount.class);
        }else{
            return new ArrayList<>();
        }
    }
}
