package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 部门 Repository
 * @author wuwentan
 * @date 2019/4/11
 */
public interface DeptRepository extends JpaRepository<Department, String> {

    /**
     * 根据企业平台Id查询所有部门列表并根据序号倒排
     * @param corpId
     * @return
     */
    List<Department> findAllByCorpIdOrderByNumDesc(String corpId);

    /**
     * 通过名称查询对应的
     * @param name
     * @return
     */
    Optional<Department> findByName(String name);
}
