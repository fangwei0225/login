package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.ItemtypeRepository;
import com.taoding.mp.base.entity.Itemtype;
import com.taoding.mp.base.service.ItemtypeService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ItemtypeServiceImpl implements ItemtypeService {

    @Autowired
    ItemtypeService itemtypeService;
    @Autowired
    ItemtypeRepository itemtypeRepository;

    @Override
    public Itemtype save(Itemtype itemtype) {
        itemtype.setCreateTime(CommonUtils.getStringDate(new Date()));
        itemtype.setId(CommonUtils.getUUID());
        return itemtypeRepository.save(itemtype);
    }

    @Override
    public Itemtype update(Itemtype itemtype) {
        if (StringUtils.isNotBlank(itemtype.getId())) {
            Itemtype item = itemtypeRepository.findById(itemtype.getId()).orElse(null);
            UpdateUtils.copyNonNullProperties(item, itemtype);
        }
        return itemtypeRepository.saveAndFlush(itemtype);
    }

    @Override
    public Itemtype findById(String id) {
        return null;
    }

    /***
     * 查询出类型列表
     * @return
     */
    @Override
    public List<Itemtype> findByList() {
        return itemtypeRepository.findAll();
    }
}
