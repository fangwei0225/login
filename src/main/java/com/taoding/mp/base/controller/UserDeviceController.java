package com.taoding.mp.base.controller;


import com.taoding.mp.base.entity.UserDevice;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.UserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 用户-设备表 Controller
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@RestController
@RequestMapping(value = "/server/userDevice")
public class UserDeviceController {

    @Autowired
    private UserDeviceService userDeviceService;

    /**
     * 保存用户-设备表
     *
     * @param userDevice
     * @return UserDevice
     */
    @PostMapping(value = "/save")
    public ResponseVO<UserDevice> save(@RequestBody UserDevice userDevice) {
        return new ResponseVO<>(userDeviceService.save(userDevice));
    }

    /**
     * 更新用户-设备表
     *
     * @param userDevice
     * @return UserDevice
     */
    @PostMapping(value = "/update")
    public ResponseVO<UserDevice> update(@RequestBody UserDevice userDevice) {
        return new ResponseVO<>(userDeviceService.update(userDevice));
    }

    /**
     * 查询用户-设备表列表
     *
     * @return 用户-设备表List
     */
    @PostMapping(value = "/findByList")
    public ResponseVO<List<UserDevice>> findByList() {
        return new ResponseVO<>(userDeviceService.findByList());
    }

    /**
     * 查询用户-设备表信息
     *
     * @param id 用户-设备表Id
     * @return 用户-设备表
     */
    @GetMapping(value = "/findById")
    public ResponseVO<UserDevice> findById(String id) {
        return new ResponseVO<>(userDeviceService.findById(id));
    }

    /**
     * 带条件分页查询用户-设备表列表
     *
     * @param params 用户-设备表
     * @return 用户-设备表
     */
    @PostMapping("/findByPage")
    public ResponseVO<PageVO<UserDevice>> findByPage(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(userDeviceService.findByPage(params));
    }

    /**
     * 删除用户-设备表
     *
     * @param id
     * @return UserDevice
     */
    @GetMapping(value = "/deleteById")
    public ResponseVO<UserDevice> deleteById(String id) {
        userDeviceService.deleteById(id);
        return new ResponseVO<>(null);
    }

    /**
     * 批量删除用户-设备表
     *
     * @param ids
     * @return UserDevice
     */
    @GetMapping(value = "/deleteByIds")
    public ResponseVO<UserDevice> deleteByIds(String ids) {
        userDeviceService.deleteByIds(ids);
        return new ResponseVO<>(null);
    }




}