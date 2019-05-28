package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Menu;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MenuService;
import com.taoding.mp.base.service.SessionService;
import com.taoding.mp.base.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户管理
 * @author wuwentan
 * @date 2018/8/9
 */
@RestController
@RequestMapping("/server/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping("/page")
    public ResponseVO<PageVO<User>> page(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(userService.getPage(params));
    }

    @RequestMapping("/save")
    public ResponseVO<User> save(@RequestBody User user) {
        User returnUser = userService.save(user);
        // returnUser.setPassword("");

        // 同步更新缓存中用户信息
        sessionService.updateUserSessionByUserId(user.getId());
        return new ResponseVO<>(returnUser);
    }

    @RequestMapping("/verifyName")
    public ResponseVO<Boolean> verifyName(@RequestParam(required = false) String username){
        if(StringUtils.isNotBlank(username)){
            boolean bool = userService.verifyName(username);
            return new ResponseVO(bool);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数username");
        }
    }

    @RequestMapping("/info")
    public ResponseVO<User> info(@RequestParam(required = false) String id) {
        if(StringUtils.isNotBlank(id)){
            User user = userService.getById(id);
            return new ResponseVO<>(user);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    @RequestMapping("/delete")
    public ResponseVO delete(@RequestParam(required = false) String id) {
        if(StringUtils.isNotBlank(id)) {
            userService.deleteById(id);
            return new ResponseVO<>("操作成功");
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
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
        return new ResponseVO<>(resultList);
    }

    /**
     * 重置密码
     * @param id
     */
    @GetMapping(value = "/resetPwd")
    public void resetPassword(String id) {
        User user = new User();
        user.setId(id);
        user = userService.resetPwd(user);
        //清空用户session
        sessionService.removeUserSessionByUserId(user.getId());
    }

    /**
     * 修改密码
     * @param params
     */
    @PostMapping(value = "/updatePwd")
    public ResponseVO<User> modifyPwd(@RequestBody Map<String, String> params) {
        User user = userService.modifyPwd(params);
        ResponseVO<User> responseVO = new ResponseVO<>(user);
        if (user == null) {
            responseVO.setStatus(500);
            responseVO.setMsg("原密码输入错误");
        } else {
            //清空用户session
            sessionService.removeUserSessionByUserId(user.getId());
        }

        return responseVO;
    }

    /**
     * 查询所有区级领导列表
     * @return
     */
    @PostMapping(value = "/listByLeader")
    public ResponseVO<List<User>> listByLeader(){
        List<User> list = userService.listByLeader();
        return new ResponseVO<>(list);
    }

    /**
     * 查询所有企业用户列表
     * @return
     */
    @PostMapping(value = "/listByCompany")
    public ResponseVO<List<User>> listByCompany(){
        List<User> list = userService.listByCompany();
        return new ResponseVO<>(list);
    }
}
