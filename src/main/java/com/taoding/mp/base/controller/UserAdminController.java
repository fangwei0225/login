package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Menu;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MenuService;
import com.taoding.mp.base.service.UserAdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户
 * @author wuwentan
 * @date 2019/4/11
 */
@RestController
@Slf4j
@RequestMapping("/server/userAdmin")
public class UserAdminController {

    @Autowired
    UserAdminService userAdminService;

    @Autowired
    private MenuService menuService;

    /**
     * 分页查询用户列表
     * @param params
     * @return
     */
    @RequestMapping("/page")
    public PageVO<User> page(@RequestBody Map<String,String> params) {
        return userAdminService.getPage(params);
    }

    /**
     * 验证用户名是否重复
     * @param userName
     * @return
     */
    @GetMapping("/verifyName")
    public ResponseVO<Boolean> verifyName(@RequestParam(required = false) String  userName){
        if(StringUtils.isBlank(userName)){
            log.error("verifyName(): param is null");
            return new ResponseVO<>(400, "用户名不能为空");
        }
        Boolean verifyName = userAdminService.verifyName(userName);
        return new ResponseVO(verifyName);
    }

    @RequestMapping("/save")
    public ResponseVO<User> save(@RequestBody User user){
        User returnUser = userAdminService.save(user);
        // returnUser.setPassword("");
        return new ResponseVO<>(returnUser);
    }

    @RequestMapping("/info")
    public ResponseVO<User> info(@RequestParam(value = "id")String id){
        User user = userAdminService.getById(id);
        return new ResponseVO<>(user);
    }

    @RequestMapping("/delete")
    public ResponseVO delete(@RequestParam(value = "id")String id){
        userAdminService.deleteById(id);
        return new ResponseVO<>("");
    }

    /**
     * 根据用户加载对应的权限菜单
     * @return
     */
    @GetMapping(value = "/loadMenuListByUser")
    public ResponseVO<List<Menu>> loadMenuListByUser() {
        UserSession userSession = UserSession.getUserSession();
        List<Menu> resultList = new ArrayList<>();
        if (StringUtils.isNotBlank(userSession.getUserId())) {
            resultList = menuService.findListByUserId(userSession.getUserId());
        }
        return new ResponseVO(resultList);
    }

    /**
     * 重置密码
     * @param id
     */
    @GetMapping(value = "/resetPwd")
    public void resetPassword(String id) {
        User user = new User();
        user.setId(id);
        userAdminService.resetPwd(user);
    }

    /**
     * 修改密码
     * @param params
     */
    @PostMapping(value = "/updatePwd")
    public ResponseVO modifyPwd(@RequestBody Map<String, String> params) {
        User user = userAdminService.modifyPwd(params);
        if (user == null) {
            return new ResponseVO(500,"旧密码有误","");
        } else {
            return new ResponseVO("密码修改成功");
        }
    }

    /**
     * 查询所有管理员用户列表
     * @return
     */
    @PostMapping(value = "/findByList")
    public ResponseVO<List<User>> findByList() {
        return new ResponseVO<>(userAdminService.findByList());
    }

}
