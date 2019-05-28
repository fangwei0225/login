package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Itemtype;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户类型
 */
public interface ItemtypeRepository  extends JpaRepository<Itemtype,String>{
}
