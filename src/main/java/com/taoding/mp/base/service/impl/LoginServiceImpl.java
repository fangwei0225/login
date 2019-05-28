package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.LoginCheck;
import com.taoding.mp.base.service.DeptService;
import com.taoding.mp.base.service.LoginService;
import com.taoding.mp.base.service.UserService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录验证接口类
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    UserService userService;
    @Autowired
    DeptService deptService;

    @Override
    public LoginCheck checkLogin(User loginUser) {
        LoginCheck check = new LoginCheck();

        User user = userService.getByUsername(loginUser.getUsername(), loginUser.getCorpId());
        if (user != null) {
            String loginPassword = CommonUtils.md5Encode(loginUser.getPassword()).toLowerCase();
            if (loginPassword.equals(user.getPassword())) {
                UpdateUtils.copyNonNullProperties(user, loginUser);
                // 接口返回时将密码字段置空
                // loginUser.setPassword("");
                // 获取用户所在部门名称
                String deptName = deptService.getDeptNames(user.getDeptId());
                loginUser.setDeptName(deptName);
                check.setUser(loginUser);
                check.setCode(200);
                check.setMessage("登录成功！");
            } else {
                check.setCode(201);
                check.setMessage("用户名或密码不正确，请检查后重试！");
            }
        } else {
            check.setCode(202);
            check.setMessage("用户名或密码不正确，请检查后重试！");
        }

        return check;
    }
}

