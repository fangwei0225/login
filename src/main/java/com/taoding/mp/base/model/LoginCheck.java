package com.taoding.mp.base.model;

import com.taoding.mp.base.entity.User;
import lombok.Data;

/**
 * 登录校验返回数据对象
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@Data
public class LoginCheck {

    private Integer code;

    private String message;

    private User user;

    private String sessionId;
}
