package com.taoding.mp.core.datetime.entity;

import com.taoding.mp.base.model.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description: 日历表
 * @date 2019/5/7 000715:00
 */
@Data
@Entity
@Table(name = "kalendar")
public class Kalendar  extends BaseEntity implements Serializable {

    /**
     * 所在年份
     */
    private Integer year;
    /**
     * 日期
     */
    private String date;
    /**
     * 本周星期数 1.周天，2。周一  。。。。。。。。
     */
    private int week;
    /**
     * 0 正常工作日（正常上班时间），1.周末节假日（周日，周天） 2，法定节假日（各种小长假）
     */
    private Integer type;
}
