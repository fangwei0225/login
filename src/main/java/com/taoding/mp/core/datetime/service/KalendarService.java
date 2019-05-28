package com.taoding.mp.core.datetime.service;

import com.taoding.mp.core.datetime.entity.Kalendar;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/7 000718:52
 */
public interface KalendarService {


    /**
     * 列表
     * @param year
     * @return
     */
    List<Kalendar> list(Integer year);

    /**
     * 添加接口
     * @param dates
     * @return
     */
    boolean add(Integer year,String dates);

    /**
     * 根据type查询所有的数据
     * @return
     */
    List<Kalendar> allList(Integer type);
}
