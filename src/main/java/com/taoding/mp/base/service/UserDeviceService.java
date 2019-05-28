/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.UserDevice;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 用户-设备表 Service
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface UserDeviceService {

    /**
     * 查询用户-设备表信息
     *
     * @param id 用户-设备表Id
     * @return 用户-设备表
     */
    UserDevice findById(String id);

    /**
     * 查询用户-设备表列表
     *
     * @return 用户-设备表List
     */
    List<UserDevice> findByList();

    /**
     * 带条件分页查询用户-设备表列表
     *
     * @param params 用户-设备表
     * @return 用户-设备表PageVO
     */
    PageVO<UserDevice> findByPage(Map<String, String> params);

    /**
     * 保存用户-设备表
     *
     * @param userDevice 用户-设备表
     * @return 用户-设备表
     */
     UserDevice save(UserDevice userDevice);

    /**
     * 更新用户-设备表
     *
     * @param userDevice 用户-设备表
     * @return 用户-设备表
     */
     UserDevice update(UserDevice userDevice);

    /**
     * 删除用户-设备表
     *
     * @param id
     * @return
     */
    int deleteById(String id);

    /**
     * 批量删除用户-设备表
     *
     * @param ids
     * @return
     */
    int deleteByIds(String ids);

    /**
     * 根据用户删除设备
     *
     * @param userId
     * @return
     */
    int deleteByUserId(String userId);
}