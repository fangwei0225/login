package com.taoding.mp.base.model.push;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 应用内消息。或者称作：自定义消息，透传消息。
 * 此部分内容不会展示到通知栏上，JPush SDK 收到消息内容后透传给 App。需要 App 自行处理。
 *
 * @author Leon
 * @version 2019/4/17 10:15
 */
@Data
public class Message {

    /**
     * 必选,消息内容本身
     */
    private String msg_content;

    /**
     * 可选,消息标题
     */
    private String title;

    /**
     * 可选,消息内容类型
     */
    private String content_type;

    /**
     * JSON 格式的可选参数
     */
    private JSONObject extras;

}
