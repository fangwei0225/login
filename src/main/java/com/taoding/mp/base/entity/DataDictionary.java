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
 * 数据字典
 * @author wuwentan
 * @date 2019/3/7
 */
@Data
@Entity
@Table(name = "data_dictionary")
public class DataDictionary extends BaseEntity {

    /**
     * 字典分类
     */
    @Column
    private String type;

    /**
     * 字典内容
     */
    @Column
    private String name;

    /**
     * 上级字典id
     */
    @Column
    private String parentId;

    /**
     * 排序号
     */
    @Column
    private Integer num;
    /**
     * 属性值
     */
    @Column
    private Integer dataValue;
    /**
     * 备注
     */
    @Column
    private String remark;

    /**
     * 子级数据集合，前端展示用
     */
    @Transient
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private List<DataDictionary> children = new ArrayList<>();

    /**
     * 配合前端展示插件字段
     * @return
     */
    public String getText(){
        return this.name;
    }

    /**
     * 配合前端展示插件字段
     * @return
     */
    public String getValue(){
        return super.getId();
    }

}
