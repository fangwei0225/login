package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Corp;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 企业信息管理接口类
 *
 * @author wuwentan
 * @date 2018/11/7
 */
public interface CorpService {

    /**
     * 分页查询企业信息列表
     *
     * @param params
     * @return
     */
    PageVO<Corp> getPage(Map<String, String> params);

    /**
     * 查询企业信息列表
     *
     * @param params
     * @return
     */
    List<Corp> getList(Map<String, String> params);

    /**
     * 保存企业信息
     *
     * @param corp
     * @return
     */
    Corp save(Corp corp);

    /**
     * 根据id查询企业信息
     *
     * @param id
     * @return
     */
    Corp getById(String id);

    /**
     * 根据id删除企业信息
     *
     * @param id
     */
    void deleteById(String id);
}
