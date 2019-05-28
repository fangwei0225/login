package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 部门管理接口
 * @author wuwentan
 * @date 2019/4/11
 */
public interface DeptService {

    /**
     * 查询所有部门（根据序号倒排）
     * @return
     */
    List<Department> listAll();

    /**
     * 分页查询部门列表（根据序号倒排）
     * @param params
     * @return
     */
    PageVO<Department> getPage(Map<String, String> params);

    /**
     * 新增或修改部门信息
     * @param dept
     * @return
     */
    Department save(Department dept);

    /**
     * 根据id查询部门信息
     * @param id
     * @return
     */
    Department getById(String id);

    /**
     * 根据id删除部门信息
     * @param id
     */
    void deleteById(String id);

    /**
     * 根据parentId查询下一级节点列表
     * @param parentId
     * @return
     */
    List<Department> listByPid(String parentId);

    /**
     * 根据部门ids返回部门名称，逗号分隔
     * @param ids
     * @return
     */
    String getDeptNameByIds(String ids);

    /**
     * 获取部门名称（部门-科室）
     * @param id
     * @return
     */
    String getDeptNames(String id);

    /**
     * 获取上级部门id
     * @param id
     * @return
     */
    String getParentId(String id);

}
