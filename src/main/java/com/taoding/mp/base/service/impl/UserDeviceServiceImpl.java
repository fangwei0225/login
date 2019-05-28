/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */
package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.UserDeviceRepository;
import com.taoding.mp.base.entity.UserDevice;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.service.UserDeviceService;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 用户-设备表 ServiceImpl
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@Service
public class UserDeviceServiceImpl extends BaseDAO implements UserDeviceService {

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    /**
     * 查询用户-设备表信息
     *
     * @param id 用户-设备表Id
     * @return 用户-设备表
     */
    @Override
    public UserDevice findById(String id) {
        return userDeviceRepository.findById(id).orElse(null);
    }

    /**
     * 查询用户-设备表列表
     *
     * @return 用户-设备表List
     */
    @Override
    public List<UserDevice> findByList() {
        return userDeviceRepository.findAll();
    }

    /**
     * 保存用户-设备表
     *
     * @param userDevice 用户-设备表
     * @return 用户-设备表
     */
     @Override
     public UserDevice save(UserDevice userDevice) {
         if (StringUtils.isBlank(userDevice.getUserId()) || StringUtils.isBlank(userDevice.getAlias())) {
             throw new CustomException(500, "userId和alias不能为空，否则无法完成绑定！");
         }
         String sql = " DELETE FROM user_device WHERE user_id = ? OR alias = ? ";
         jdbc.update(sql, userDevice.getUserId(), userDevice.getAlias());
         setBaseProperties(userDevice);
         return userDeviceRepository.save(userDevice);
     }

    /**
     * 通用设置
     *
     * @param userDevice
     */
     private void setBaseProperties(UserDevice userDevice) {
         userDevice.setId(CommonUtils.getUUID());
         userDevice.setCreateTime(CommonUtils.getStringDate(new Date()));
         userDevice.setUpdateTime(CommonUtils.getStringDate(new Date()));
         // TODO
         // userDevice.setCorpId(null);
         userDevice.setIsDelete(1);
     }

    /**
     * 更新用户-设备表
     *
     * @param userDevice 用户-设备表
     * @return 用户-设备表
     */
     @Override
     public UserDevice update(UserDevice userDevice) {
         if (StringUtils.isNotBlank(userDevice.getId())) {
             UserDevice sourceUserDevice = userDeviceRepository.findById(userDevice.getId()).orElse(null);
             UpdateUtils.copyNonNullProperties(sourceUserDevice, userDevice);
         }
         return userDeviceRepository.saveAndFlush(userDevice);
     }

    /**
     * 带条件分页查询用户-设备表列表
     *
     * @param params  用户-设备表
     * @return PageVO
     */
    @Override
    public PageVO<UserDevice> findByPage(Map<String, String> params) {
        int pageNo = null == params.get("pageNo") ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = null == params.get("pageSize") ? 15 : Integer.parseInt(params.get("pageSize"));
        List<Object> args = new ArrayList<>();
        String sql = " SELECT t1.* FROM user_device t1 "
                   + " WHERE 1=1 "
                   + " ORDER BY t1.create_time DESC";
        // TODO
        return getPage(sql, pageNo, pageSize, args, new BeanPropertyRowMapper(UserDevice.class));
    }

    /**
     * 删除用户-设备表
     *
     * @param id
     * @return
     */
    @Override
    public int deleteById(String id) {
        userDeviceRepository.deleteById(id);
        return 1;
    }

    /**
     * 批量删除用户-设备表
     *
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(String ids) {
        Stream.of(StringUtils.split(ids, ",")).forEach(userDeviceRepository::deleteById);
        return 1;
    }

    /**
     * 根据用户删除设备
     *
     * @param userId
     * @return
     */
    @Override
    public int deleteByUserId(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            userDeviceRepository.deleteUserDeviceByUserIdEquals(userId);
        }
        return 1;
    }
}