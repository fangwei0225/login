package com.taoding.mp.core.company.dao;

import com.taoding.mp.core.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wuwentan
 * @date 2019/5/7
 */
public interface CompanyRepository extends JpaRepository<Company,String> {
}
