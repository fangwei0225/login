package com.taoding.mp.base.model.push;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 通知内容体。是被推送到客户端的内容。与 message 一起二者必须有其一，可以二者并存
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
@Data
public class Notification {

    /**
     * 如果多个平台通知alert都一样则使用该字段
     */
    private String alert;

    /**
     * 安卓平台的通知内容
     */
    private JSONObject android;

    /**
     * ios平台通知内容
     */
    private JSONObject ios;

}
