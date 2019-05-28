/**
 * Copyright © 2019, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.model;

import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.entity.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述
 *
 * @author Leon
 * @version 2019/5/21 18:10
 */
@Accessors(chain = true)
@Data
public class MessageDTO {

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 业务数据
     */
    private JSONObject extra;

    /**
     * 消息类型
     */
    private String category;

    /**
     * 业务链接
     */
    private String url;

    /**
     * 消息推送者
     */
    private List<User> receivers = new ArrayList<>();

}
