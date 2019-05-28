package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 菜单 Repository
 *
 * @author Leon
 * @version 2018/11/6 14:49
 */
public interface MenuRepository extends JpaRepository<Menu, String> {
}
