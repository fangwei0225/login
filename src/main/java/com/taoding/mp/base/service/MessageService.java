/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.model.MessageDTO;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 消息 Service
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface MessageService {

    /**
     * 查询消息信息
     *
     * @param id 消息Id
     * @return 消息
     */
    Message findById(String id);

    /**
     * 查询消息列表
     *
     * @return 消息List
     */
    List<Message> findByList();

    /**
     * 带条件分页查询消息列表
     *
     * @param params 消息
     * @return 消息PageVO
     */
    PageVO<Message> findByPage(Map<String, String> params);

    /**
     * 保存消息
     *
     * @param message 消息
     * @return 消息
     */
     Message save(Message message);

    /**
     * 更新消息
     *
     * @param message 消息
     * @return 消息
     */
     Message update(Message message);

    /**
     * 删除消息
     *
     * @param id
     * @return
     */
    int deleteById(String id);

    /**
     * 批量删除消息
     *
     * @param ids
     * @return
     */
    int deleteByIds(String ids);

    /**
     * 创建并保存消息
     *
     * @param messageDTO
     * @return
     */
    Message buildAndSaveMessage(MessageDTO messageDTO);


    /**
     * 创建并保存消息记录
     *
     * @param message
     * @param messageDTO
     * @return
     */
    List<MessageReceiver> buildAndMessageReceiver(Message message, MessageDTO messageDTO);
}