package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Itemtype;
import com.taoding.mp.base.model.ResponseVO;

import java.util.List;

public interface ItemtypeService {

    /**
     * 保存
     *
     * @param itemtype
     * @return
     */
    Itemtype save(Itemtype itemtype);

    /**
     * 更新
     *
     * @param itemtype
     * @return
     */
    Itemtype update(Itemtype itemtype);

    /**
     * 登录用户类型
     * @param id
     * @return
     */
     Itemtype findById(String id);

    /**
     * 查询用户类型列表
     *
     * @return 类型List
     */
     List<Itemtype> findByList();
}
