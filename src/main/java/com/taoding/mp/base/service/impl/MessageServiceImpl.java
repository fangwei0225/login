/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */
package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.MessageReceiverRepository;
import com.taoding.mp.base.dao.MessageRepository;
import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.MessageDTO;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MessageService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 消息 ServiceImpl
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@Service
public class MessageServiceImpl extends BaseDAO implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReceiverRepository messageReceiverRepository;

    /**
     * 查询消息信息
     *
     * @param id 消息Id
     * @return 消息
     */
    @Override
    public Message findById(String id) {
        return messageRepository.findById(id).orElse(null);
    }

    /**
     * 查询消息列表
     *
     * @return 消息List
     */
    @Override
    public List<Message> findByList() {
        return messageRepository.findAll();
    }

    /**
     * 保存消息
     *
     * @param message 消息
     * @return 消息
     */
     @Override
     public Message save(Message message) {
         setBaseProperties(message);
         return messageRepository.save(message);
     }

    /**
     * 通用设置
     *
     * @param message
     */
     private void setBaseProperties(Message message) {
         message.setId(CommonUtils.getUUID());
         message.setCreateTime(CommonUtils.getStringDate(new Date()));
         message.setUpdateTime(CommonUtils.getStringDate(new Date()));
         message.setCorpId(UserSession.getUserSession().getCorpId());
     }

    /**
     * 更新消息
     *
     * @param message 消息
     * @return 消息
     */
     @Override
     public Message update(Message message) {
         if (StringUtils.isNotBlank(message.getId())) {
             Message sourceMessage = messageRepository.findById(message.getId()).orElse(null);
             UpdateUtils.copyNonNullProperties(sourceMessage, message);
         }
         return messageRepository.saveAndFlush(message);
     }

    /**
     * 带条件分页查询消息列表
     *
     * @param params  消息
     * @return PageVO
     */
    @Override
    public PageVO<Message> findByPage(Map<String, String> params) {
        int pageNo = null == params.get("pageNo") ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = null == params.get("pageSize") ? 15 : Integer.parseInt(params.get("pageSize"));
        List<Object> args = new ArrayList<>();
        String sql = " SELECT t1.* FROM message t1 "
                   + " WHERE 1=1 "
                   + " ORDER BY t1.create_time DESC";
        // TODO
        return getPage(sql, pageNo, pageSize, args, new BeanPropertyRowMapper(Message.class));
    }

    /**
     * 删除消息
     *
     * @param id
     * @return
     */
    @Override
    public int deleteById(String id) {
        messageRepository.deleteById(id);
        return 1;
    }

    /**
     * 批量删除消息
     *
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(String ids) {
        Stream.of(StringUtils.split(ids, ",")).forEach(messageRepository::deleteById);
        return 1;
    }


    /**
     * 创建并保存消息
     *
     * @param messageDTO
     * @return
     */
    @Override
    public Message buildAndSaveMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setId(CommonUtils.getUUID());
        message.setIsDelete(1);
        message.setCreateTime(CommonUtils.getStringDate(new Date()));
        message.setTitle(messageDTO.getTitle());
        message.setMsg(messageDTO.getMsg());
        message.setExtra(messageDTO.getExtra().toJSONString());
        message.setCategory(messageDTO.getCategory());
        message.setUrl(messageDTO.getUrl());
        return messageRepository.save(message);
    }

    /**
     * 创建并保存消息记录
     *
     * @param message
     * @param messageDTO
     * @return
     */
    @Override
    public List<MessageReceiver> buildAndMessageReceiver(Message message, MessageDTO messageDTO) {
        List<User> receivers = messageDTO.getReceivers();
        List<MessageReceiver> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receivers)) {
            for (User receiver : receivers) {
                MessageReceiver mr = new MessageReceiver();
                mr.setId(CommonUtils.getUUID());
                mr.setStatus(0);
                mr.setMessageId(message.getId());
                mr.setUserId(receiver.getId());
                mr.setCreateTime(CommonUtils.getStringDate(new Date()));
                mr.setIsDelete(1);
                messageReceiverRepository.save(mr);
                result.add(mr);
            }
        }
        return result;
    }


}