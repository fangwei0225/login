package com.taoding.mp.core.company.service;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.core.company.entity.Company;

import java.util.Map;

/**
 * 企业信息（项目单位）接口类
 * @author wuwentan
 * @date 2019/5/7
 */
public interface CompanyService {

    /**
     * 分页查询企业信息列表
     * @param params
     * @return
     */
    PageVO<Company> getPage(Map<String,String> params);

    /**
     * 新增、修改企业信息
     * @param company
     * @return
     */
    Company save(Company company);

    /**
     * 根据id查询企业信息
     * @param id
     * @return
     */
    Company getById(String id);

    /**
     * 根据id删除企业信息
     * @param id
     */
    void deleteById(String id);
}
