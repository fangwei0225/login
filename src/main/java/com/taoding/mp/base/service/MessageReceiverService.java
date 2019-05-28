/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.model.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 消息记录表 Service
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface MessageReceiverService {

    /**
     * 查询消息记录表信息
     *
     * @param id 消息记录表Id
     * @return 消息记录表
     */
    MessageReceiver findById(String id);

    /**
     * 查询消息记录表列表
     *
     * @return 消息记录表List
     */
    List<MessageReceiver> findByList();

    /**
     * 带条件分页查询消息记录表列表
     *
     * @param params 消息记录表
     * @return 消息记录表PageVO
     */
    PageVO<MessageReceiver> findByPage(Map<String, String> params);

    /**
     * 保存消息记录表
     *
     * @param messageReceiver 消息记录表
     * @return 消息记录表
     */
     MessageReceiver save(MessageReceiver messageReceiver);

    /**
     * 更新消息记录表
     *
     * @param messageReceiver 消息记录表
     * @return 消息记录表
     */
     MessageReceiver update(MessageReceiver messageReceiver);

    /**
     * 删除消息记录表
     *
     * @param id
     * @return
     */
    int deleteById(String id);

    /**
     * 批量删除消息记录表
     *
     * @param ids
     * @return
     */
    int deleteByIds(String ids);

    /**
     * 根据userId查询未读信息数量
     *
     * @param userId
     * @return
     */
    int findUnreadCountByUserId(String userId);

    /**
     * 批量已读
     *
     * @param ids
     */
    void batchReadMessage(String ids);

    /**
     * 全部已读
     */
    void allReadMessage();
}