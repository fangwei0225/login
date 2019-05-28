/**
 * Copyright © 2019, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.model.MessageDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The service of websocket for supporting h5,pc,etc
 *
 * @author Leon
 * @version 2019/4/19 11:35
 */
@ServerEndpoint(value = "/webSocket/{userId}")
@Component
public class WebSocketService {

    private static final Logger lg = LoggerFactory.getLogger(WebSocketService.class);

    private static Map<String, WebSocketService> webSocketMap = new ConcurrentHashMap<>();

    private Session session;

    @OnOpen
    public void onOpen(@PathParam(value = "userId") String userId, Session session) {
        if (StringUtils.isNotBlank(userId)) {
            this.session = session;
            WebSocketService old = webSocketMap.put(userId, this);
            if (null != old) {
                try {
                    if(old.session.isOpen()){
                        old.session.close();
                    }
                } catch (IOException e) {
                    lg.error("关闭旧的连接失败, {}", e.getMessage());
                }
            }
            lg.info("{} 用户建立连接成功！", userId);
        }
    }

    @OnClose
    public void onClose(@PathParam(value = "userId") String userId) {
        if (StringUtils.isNotBlank(userId)) {
            webSocketMap.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        lg.error("信息发生错误：{}", error.getMessage());
    }

    @OnMessage
    public void onMessage(@PathParam(value = "userId") String userId, String message, Session session) {
        lg.info("收到客户端【{}】的消息【{}】", userId, message);
    }

    /**
     * 消息推送
     *
     * @param message
     * @param messageDTO
     * @param messageReceivers
     */
    public void pushMessage(Message message, MessageDTO messageDTO, List<MessageReceiver> messageReceivers) {
        JSONObject extra = messageDTO.getExtra();
        extra.put("title", messageDTO.getTitle());
        extra.put("msg", messageDTO.getMsg());
        for (MessageReceiver messageReceiver : messageReceivers) {
            extra.put("noticeId", messageReceiver.getId());
            pushMessage(messageReceiver.getUserId(), (JSONObject)extra.clone());
        }
    }

    public void pushMessage(String userId, Map<String, Object> message) {
        WebSocketService webSocketService = webSocketMap.get(userId);
        if (null != webSocketService) {
            try {
                webSocketService.sendMessage(JSON.toJSONString(message));
            } catch (IOException e) {
                lg.error("【Web端发送消息失败】，错误原因：{}  消息：{}", e.getMessage(), message);
            }
            lg.info("【Web端发送消息成功】，消息接受者：{} 信息：{}", userId, message);
        } else {
            lg.warn("【Web端发送消息失败】，原因：用户未建立连接！");
        }
    }

    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

}
