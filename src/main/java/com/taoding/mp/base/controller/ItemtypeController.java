package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Itemtype;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.ItemtypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户与类型管理
 *
 * @author Leon
 * @version 2018/11/6 14:46
 */
@RestController
@RequestMapping(value = "/server/itemtype")
public class ItemtypeController {
    @Autowired
    ItemtypeService itemtypeService;

    /**
     * 保存或更新
     *
     * @param itemtype
     * @return
     */
    @PostMapping(value = "/save")
    public ResponseVO save(@RequestBody Itemtype itemtype) {
        if (StringUtils.isNotBlank(itemtype.getId())) {
            itemtypeService.update(itemtype);
        } else {
            itemtypeService.save(itemtype);
        }
        return new ResponseVO(itemtype);
    }
    /**
     * 菜单数据列表
     *
     * @return
     */
    @GetMapping(value = "/findByList")
    public ResponseVO findByList() {
        return new ResponseVO(itemtypeService.findByList());
    }

}
