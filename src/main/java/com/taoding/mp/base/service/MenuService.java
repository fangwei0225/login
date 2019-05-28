package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Menu;
import com.taoding.mp.base.model.ResponseVO;

import java.util.List;

/**
 * 菜单 Service
 *
 * @author Leon
 * @version 2018/11/6 14:47
 */
public interface MenuService {

    /**
     * 保存
     *
     * @param menu
     * @return
     */
    Menu save(Menu menu);

    /**
     * 更新
     *
     * @param menu
     * @return
     */
    Menu update(Menu menu);

    /**
     * 删除
     *
     * @param id
     */
    ResponseVO deleteById(String id);

    /**
     * 查询一个
     *
     * @param id
     * @return
     */
    Menu findById(String id);


    /**
     * 查询所有
     *
     * @return
     */
    List<Menu> findByList();

    /**
     * 根据用户加载相应的菜单
     *
     * @param userId
     * @return
     */
    List<Menu> findListByUserId(String userId);

    /**
     * 加载目录
     *
     * @return
     */
    List<Menu> findTreeList();

    /**
     * Validate parentId is legal.Current menu's parentId cannot be ids of children
     *
     * @param menuId
     * @param parentId
     * @return
     */
    boolean validateParentIdOfMenu(String menuId, String parentId);

}
