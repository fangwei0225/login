package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author wuwentan
 * @date 2018/8/9
 */
public interface UserRepository extends JpaRepository<User, String> {

    User findByUsernameAndCorpId(String username, String corpId);

    User findByOpenId(String openId);

    List<User> findByDeptId(String deptId);
}
