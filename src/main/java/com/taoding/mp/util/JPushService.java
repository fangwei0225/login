/**
 * Copyright © 2019, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.taoding.mp.base.dao.MessageReceiverRepository;
import com.taoding.mp.base.dao.MessageRepository;
import com.taoding.mp.base.dao.UserDeviceRepository;
import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.entity.UserDevice;
import com.taoding.mp.base.model.MessageDTO;
import com.taoding.mp.base.model.push.Audience;
import com.taoding.mp.base.model.push.Notification;
import com.taoding.mp.base.model.push.Options;
import com.taoding.mp.base.model.push.PushModel;
import com.taoding.mp.base.service.ConfigService;
import com.taoding.mp.base.thread.PushThread;
import com.taoding.mp.commons.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 类功能描述
 *
 * @author Leon
 * @version 2019/4/17 14:02
 */
@Component
public class JPushService {

    private static final Logger lg = LoggerFactory.getLogger(JPushService.class);

    /**
     * 推送地址
     */
    private static final String URL = "https://bjapi.push.jiguang.cn/v3/push";

    /**
     * 极光推送APP key
     */
    private static final String APPKEY = "d1fbfe99df1640db75d8836f";

    /**
     * 极光推送masterSecret
     */
    private static final String MASTER_SECRET = "61bfe463c4b62ac101a58f54";

    /**
     * Basic认证
     */
    private static final String authorization;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RestTemplate template;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReceiverRepository messageReceiverRepository;

    @Autowired
    private WebSocketService webSocketService;


    /**
     * 单个推送
     *
     * @param message
     * @param messageDTO
     * @param messageReceivers
     */
    public void pushMessage(Message message, MessageDTO messageDTO, List<MessageReceiver> messageReceivers) {
        preCheck(messageDTO);
        for (MessageReceiver messageReceiver : messageReceivers) {
            pushMessage(message, messageDTO, messageReceiver);
        }
    }

    /**
     * 多个推送
     *
     * @param message
     * @param messageDTO
     * @param messageReceiver
     */
    public void pushMessage(Message message, MessageDTO messageDTO, MessageReceiver messageReceiver) {
        List<UserDevice> userDeviceList = userDeviceRepository.findByUserIdIn(Lists.newArrayList(messageReceiver.getUserId()));
        if (CollectionUtils.isNotEmpty(userDeviceList)) {
            JSONObject extra = (JSONObject)messageDTO.getExtra().clone();
            extra.put("noticeId", messageReceiver.getId());
            String reqContent = build(userDeviceList, messageDTO.getMsg(), messageDTO.getTitle(), extra);
            executorService.execute(new PushThread(reqContent, template , messageRepository, messageReceiverRepository, Lists.newArrayList(messageReceiver)));
        } else {
            lg.warn("【移动端推送失败】 ---> 推送用户暂无绑定的移动设备! 此次推送用户：{} ", messageReceiver.getUserId());
        }
    }

    /**
     * 推送
     *
     * @param requestBody
     * @return
     */
    public static String push(String requestBody, RestTemplate template){
        MultiValueMap<String, String> headers= new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", authorization);
        ResponseEntity<String> responseEntity = template.exchange(URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);
        return responseEntity.getBody();
    }

    private String build(List<UserDevice> userDevices, String msg, String title, JSONObject extra) {
        JSONObject ios = new JSONObject();
        //IOS角标
        ios.put("badge", "+1");
        //设置声音
        ios.put("sound", "default");
        ios.put("content-available", true);
        ios.put("extras", extra.clone());
        //设置IOS推送alert
        JSONObject iosAlertObj = new JSONObject();
        iosAlertObj.put("title", msg);
        iosAlertObj.put("body", title);
        ios.put("alert", iosAlertObj);

        // 设置Android推送
        JSONObject androidAlertObj = new JSONObject();
        // style
        androidAlertObj.put("title", msg);
        // 1表示大段文本
        androidAlertObj.put("style", 1);
        androidAlertObj.put("big_text", title);
        androidAlertObj.put("extras", extra.clone());

        PushModel model = new PushModel();

        // 设置推送目标设备
        Audience audience = new Audience();
        List<String> aliasList = new ArrayList<>();
        userDevices.stream().map(UserDevice::getAlias).forEach(aliasList::add);
        audience.setAlias(aliasList);
        model.setAudience(audience);
        // 设置推送内容
        Notification notification=new Notification();
        notification.setAlert(title);
        notification.setIos(ios);
        notification.setAndroid(androidAlertObj);
        // 设置通知内容
        model.setNotification(notification);
        // 设置极光推送IOS端的环境为开发环境
        Options options = new Options();
        options.setApns_production(iosProduction());
        model.setOptions(options);
        // 返回最终发送到极光的json
        return JSON.toJSONString(model);
    }

    /**
     * 从Redis里获取极光推送IOS环境
     *
     * @return
     */
    private boolean iosProduction(){
        String result = configService.getConfig(Constants.IOS_PRODUCTION).getValue();
        return "true".equalsIgnoreCase(result);
    }

    private void preCheck(MessageDTO messageDTO) {
        if (StringUtils.isAnyBlank(messageDTO.getMsg(), messageDTO.getTitle())) {
            throw new IllegalArgumentException("消息的title mgs 必须填写");
        }
    }

    static {
        authorization = "Basic " + Base64.getEncoder().encodeToString((APPKEY + ":" + MASTER_SECRET).getBytes());
    }
}
