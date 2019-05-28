/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户-设备表 Repository
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    List<UserDevice> findByUserIdIn(List<String> userIds);

    void deleteUserDeviceByUserIdEquals(String userId);
}