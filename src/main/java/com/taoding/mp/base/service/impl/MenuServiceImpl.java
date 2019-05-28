package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.MenuRepository;
import com.taoding.mp.base.entity.Menu;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MenuService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 菜单 Service
 *
 * @author Leon
 * @version 2018/11/6 14:48
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MenuService menuService;

    /**
     * 保存
     *
     * @param menu
     * @return
     */
    @Override
    public Menu save(Menu menu) {
        menu.setCreateTime(CommonUtils.getStringDate(new Date()));
        menu.setId(CommonUtils.getUUID());
        if (StringUtils.isBlank(menu.getParentId())) {
            menu.setParentId("0");
        }
        return menuRepository.save(menu);
    }

    /**
     * 更新
     *
     * @param menu
     * @return
     */
    @Override
    public Menu update(Menu menu) {
        if (StringUtils.isNotBlank(menu.getId())) {
            Menu sou = menuRepository.findById(menu.getId()).orElse(null);
            UpdateUtils.copyNonNullProperties(sou, menu);
        }
        return menuRepository.saveAndFlush(menu);
    }

    /**
     * Validate parentId is legal.Current menu's parentId cannot be ids of children
     *
     * @param menuId
     * @param parentId
     * @return
     */
    @Override
    public boolean validateParentIdOfMenu(String menuId, String parentId) {
        String sql = " SELECT id FROM menu WHERE parent_id = ? ";
        List<String> childrenIds = jdbc.queryForList(sql, String.class, menuId);
        return childrenIds.stream().anyMatch(id -> id.equals(parentId));
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public ResponseVO deleteById(String id) {
        if (StringUtils.isNotBlank(id)) {
            Menu menu = menuRepository.findById(id).orElse(null);
            if (null != menu) {
                if (hasChildRows(menu.getId()) || usedByOtherTb(menu.getId())) {
                    return new ResponseVO(500, "数据存在关联，无法直接删除");
                } else {
                    menuRepository.deleteById(id);
                    return new ResponseVO(200, "删除成功");
                }
            }
        }
        return new ResponseVO(500, "数据不存在无法删除");
    }

    /**
     * 校验是否有子节点
     *
     * @param id
     * @return
     */
    private boolean hasChildRows(String id) {
        if (StringUtils.isAnyBlank(id)) {
            return false;
        }
        String sql = " SELECT COUNT(*) from menu WHERE parent_id = ? ";
        int rows = jdbc.queryForObject(sql, new Object[]{id}, int.class);
        return rows > 0;
    }

    /**
     * 是否被其他角色使用
     *
     * @param menuId
     * @return
     */
    private boolean usedByOtherTb(String menuId) {
        if (StringUtils.isAnyBlank(menuId)) {
            return false;
        }
        String sql = " SELECT COUNT(*) FROM role_menu WHERE menu_id = ? ";
        int rows = jdbc.queryForObject(sql, new Object[]{menuId}, int.class);
        return rows > 0;
    }


    /**
     * 查询
     *
     * @param id
     * @return
     */
    @Override
    public Menu findById(String id) {
        return menuRepository.findById(id).orElse(null);
    }

    /**
     * 查询列表
     *
     * @return
     */
    @Override
    public List<Menu> findByList() {
        return menuRepository.findAll();
    }

    /**
     * 根据用户加载相应的菜单
     *
     * @param userId
     * @return
     */
    @Override
    public List<Menu> findListByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("登录信息已失效，请重新登录！");
        }
        List<Menu> searchList = null;
        if ("Y".equalsIgnoreCase(UserSession.getUserSession().getIsAdmin())) {
            searchList = menuService.findByList();
        } else {
            String sql = " SELECT "
                       + "     DISTINCT t1.* "
                       + " FROM "
                       + " menu t1 "
                       + " INNER JOIN role_menu t2 ON t2.menu_id = t1.id "
                       + " INNER JOIN role_user t3 ON t3.role_id = t2.role_id "
                       + " WHERE 1=1 AND t1.type = 'M' AND t3.user_id = ? ";
            searchList = jdbc.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(Menu.class));
        }
        List<Menu> resultList = new ArrayList<>();
        searchList.stream().filter(s -> s.getParentId().equals("0")).sorted(Comparator.comparing(Menu::getNum)).forEach(resultList::add);
        recursionHandle(searchList, resultList);
        return resultList;
    }

    /**
     * 加载目录
     *
     * @return
     */
    @Override
    public List<Menu> findTreeList() {
        List<Menu> searchList = findByList();
        List<Menu> resultList = new ArrayList<>();
        searchList.stream()
                .filter(s -> s.getParentId().equals("0"))
                .sorted(Comparator.comparing(Menu::getNum))
                .forEach(resultList::add);
        recursionHandle(searchList, resultList);
        return resultList;
    }

    private void recursionHandle(List<Menu> searchList, List<Menu> resultList) {
        resultList.forEach(m -> {
            if (searchList.stream().anyMatch(t -> t.getParentId().equals(m.getId()))) {
                searchList.stream().filter(s -> s.getParentId().equals(m.getId())).sorted(Comparator.comparing(Menu::getNum)).forEach(m.getChildren()::add);
            }
            recursionHandle(searchList, m.getChildren());
        });
    }
}
