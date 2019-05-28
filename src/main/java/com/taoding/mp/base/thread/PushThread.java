package com.taoding.mp.base.thread;

import com.taoding.mp.base.dao.MessageReceiverRepository;
import com.taoding.mp.base.dao.MessageRepository;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.util.JPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 消息记录表 Service
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public class PushThread implements Runnable{

    private static final Logger lg = LoggerFactory.getLogger(PushThread.class);

    /**
     * 失败尝试次数
     */
    private int retryTime = 10;

    /**
     * 保存推送记录的dao
     */
    private MessageRepository messageRepository;

    /**
     * 本次推送的记录和用户对应关系
     */
    private MessageReceiverRepository messageReceiverRepository;

    /**
     * 本次推送的记录和用户对应关系
     */
    private List<MessageReceiver> messageReceiverList;

    /**
     * 推送请求json字符串
     */
    private String requestBody;

    private RestTemplate template;


    /**
     * 构造函数
     * @param reqBody 发送给极光服务器的json字符串
     * @param rest RestTemplate 实例
     * @param messageRepository 保存推送记录的dao
     * @param messageReceiverRepository 保存推送记录和被推送用户关系的dao
     * @param messageReceiverList 本次推送的记录和用户对应关系
     */
    public PushThread(String reqBody, RestTemplate rest, MessageRepository messageRepository, MessageReceiverRepository messageReceiverRepository,
                      List<MessageReceiver> messageReceiverList) {
        this.requestBody = reqBody;
        this.template = rest;
        this.messageRepository = messageRepository;
        this.messageReceiverRepository = messageReceiverRepository;
        this.messageReceiverList = messageReceiverList;
    }

    @Override
    public void run() {
        for (int i = 0; i < retryTime; i++){
            boolean b = push();
            if(b) {
                break;
            }
        }
    }

    /**
     * 像极光服务端发送rest请求,进行推送
     * @return
     */
    private boolean push(){
        boolean b = false;
        String result;
        try {
            result = JPushService.push(this.requestBody, this.template);
            b = true;
        }catch (Exception e){
            lg.error("极光推送异常：{} 请求body为：{}", e.getMessage(), requestBody);
            result = e.getMessage();
        }
        String logContent = requestBody;
        if (requestBody.length() > 1000) {
            logContent = requestBody.substring(0, 1000);
        }
        lg.info("本地推送内容为：{}， 推送结果：{}", logContent, result);
        return b;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }
}
