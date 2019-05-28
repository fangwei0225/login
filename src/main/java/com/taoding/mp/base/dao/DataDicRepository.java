package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.DataDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 数据字典
 * @author wuwentan
 * @date 2019/3/7
 */
public interface DataDicRepository extends JpaRepository<DataDictionary,String> {

    DataDictionary findByTypeAndDataValueAndIsDelete(String type,Integer value,Integer isDelete);
}
