package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.DataDictionary;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 数据字典接口类
 * @author wuwentan
 * @date 2019/3/7
 */
public interface DataDicService {

    /**
     * 分页查询数据字典列表
     * @param params
     * @return
     */
    PageVO<DataDictionary> page(Map<String, String> params);

    /**
     * 查询数据字典列表
     * @param type
     * @param parentId
     * @return
     */
    List<Map<String,Object>> list(String type, String parentId);

    /**
     * 查询数据字典树形结构列表
     * @return
     */
    List<DataDictionary> treeList(String name, String type);

    /**
     * 保存数据字典
     * @param dd
     * @return
     */
    DataDictionary save(DataDictionary dd);

    /**
     * 根据id查询数据字典
     * @param id
     * @return
     */
    DataDictionary getById(String id);

    /**
     * 根据id删除数据字典
     * @param id
     */
    void deleteById(String id);

}
