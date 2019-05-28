package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Role;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色权限管理
 *
 * @author Leon
 * @version 2018/11/6 14:45
 */
@RestController
@RequestMapping(value = "/server/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 保存或更新
     *
     * @param role
     * @return
     */
    @PostMapping(value = "/save")
    public ResponseVO save(@RequestBody Role role) {
        if (StringUtils.isNotBlank(role.getId())) {
            roleService.update(role);
        } else {
            roleService.save(role);
        }
        return new ResponseVO(role);
    }

    /**
     * 查询单个
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/findById")
    public ResponseVO findById(String id) {
        return new ResponseVO(roleService.findById(id));
    }

    /**
     * 删除
     *
     * @param id
     */
    @GetMapping(value = "/deleteById")
    public ResponseVO deleteById(String id) {
        return roleService.deleteById(id);
    }

    /**
     * 查询所有列表
     *
     * @return
     */
    @GetMapping(value = "/findByList")
    public ResponseVO findByList() {
        return new ResponseVO(roleService.findByList());
    }

    /**
     * 数据列表
     *
     * @return
     */
    @GetMapping(value = "/findTreeList")
    public ResponseVO findTreeList() {
        List<Role> searchList = roleService.findByList();
        return new ResponseVO(searchList);
    }
}
