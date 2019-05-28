package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Menu;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.MenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统菜单功能管理
 *
 * @author Leon
 * @version 2018/11/6 14:46
 */
@RestController
@RequestMapping(value = "/server/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 保存或更新
     *
     * @param menu
     * @return
     */
    @PostMapping(value = "/save")
    public ResponseVO save(@RequestBody Menu menu) {
        if (StringUtils.isNotBlank(menu.getId())) {
            if (menuService.validateParentIdOfMenu(menu.getId(), menu.getParentId())) {
                return new ResponseVO(500, "父菜单不能添加到子菜单下");
            }
            menuService.update(menu);
        } else {
            menuService.save(menu);
        }
        return new ResponseVO(menu);
    }

    /**
     * 查询单个
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/findById")
    public ResponseVO<Menu> findById(String id) {
        Menu menu = menuService.findById(id);
        if (null != menu && StringUtils.isNotBlank(menu.getParentId())) {
            Menu parentMenu = menuService.findById(menu.getParentId());
            if (null != parentMenu) {
                menu.setParentName(parentMenu.getName());
            }
        }
        return new ResponseVO(menu);
    }

    /**
     * 删除
     *
     * @param id
     */
    @GetMapping(value = "/deleteById")
    public ResponseVO deleteById(String id) {
        return menuService.deleteById(id);
    }

    /**
     * 查询所有列表
     *
     * @return
     */
    @GetMapping(value = "/findByList")
    public ResponseVO findByList() {
        return new ResponseVO(menuService.findByList());
    }

    /**
     * 菜单数据列表
     *
     * @return
     */
    @GetMapping(value = "/findTreeList")
    public ResponseVO findTreeList() {
        return new ResponseVO(menuService.findTreeList());
    }


}
