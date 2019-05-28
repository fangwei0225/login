package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.RoleUserRepository;
import com.taoding.mp.base.entity.RoleUser;
import com.taoding.mp.base.service.RoleUserService;
import com.taoding.mp.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户-角色 Service
 *
 * @author Leon
 * @version 2018/11/7 17:47
 */
@Service
public class RoleUserServiceImpl implements RoleUserService {

    @Autowired
    private RoleUserRepository roleUserRepository;

    /**
     * 保存用户-角色
     *
     * @param roleUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleUser save(RoleUser roleUser) {
        if (StringUtils.isNoneBlank(new String[]{roleUser.getRoleId(), roleUser.getUserId()})) {
            roleUser.setId(CommonUtils.getUUID());
            roleUser = roleUserRepository.save(roleUser);
        }
        return roleUser;
    }

    /**
     * 根据userId删除角色数据
     *
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUserId(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            roleUserRepository.deleteAllByUserId(userId);
        }
    }
}
