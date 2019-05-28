/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */
package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.MessageReceiverRepository;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MessageReceiverService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.JPushService;
import com.taoding.mp.util.UpdateUtils;
import com.taoding.mp.util.WebSocketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 消息记录表 ServiceImpl
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@Service
public class MessageReceiverServiceImpl extends BaseDAO implements MessageReceiverService {

    @Autowired
    private MessageReceiverRepository messageReceiverRepository;


    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JPushService jPushService;

    /**
     * 查询消息记录表信息
     *
     * @param id 消息记录表Id
     * @return 消息记录表
     */
    @Override
    public MessageReceiver findById(String id) {
        return messageReceiverRepository.findById(id).orElse(null);
    }

    /**
     * 查询消息记录表列表
     *
     * @return 消息记录表List
     */
    @Override
    public List<MessageReceiver> findByList() {
        return messageReceiverRepository.findAll();
    }

    /**
     * 保存消息记录表
     *
     * @param messageReceiver 消息记录表
     * @return 消息记录表
     */
     @Override
     public MessageReceiver save(MessageReceiver messageReceiver) {
         setBaseProperties(messageReceiver);
         messageReceiver.setStatus(0);
         return messageReceiverRepository.save(messageReceiver);
     }

    /**
     * 通用设置
     *
     * @param messageReceiver
     */
     private void setBaseProperties(MessageReceiver messageReceiver) {
         messageReceiver.setId(CommonUtils.getUUID());
         messageReceiver.setCreateTime(CommonUtils.getStringDate(new Date()));
         messageReceiver.setUpdateTime(CommonUtils.getStringDate(new Date()));
         messageReceiver.setCorpId(UserSession.getUserSession().getCorpId());
         messageReceiver.setIsDelete(1);
     }

    /**
     * 更新消息记录表
     *
     * @param messageReceiver 消息记录表
     * @return 消息记录表
     */
     @Override
     public MessageReceiver update(MessageReceiver messageReceiver) {
         if (StringUtils.isNotBlank(messageReceiver.getId())) {
             MessageReceiver sourceMessageReceiver = messageReceiverRepository.findById(messageReceiver.getId()).orElse(null);
             UpdateUtils.copyNonNullProperties(sourceMessageReceiver, messageReceiver);
         }
         messageReceiver.setStatus(1);
         messageReceiver.setFinishDate(CommonUtils.getStringDate(new Date()));
         return messageReceiverRepository.saveAndFlush(messageReceiver);
     }

    /**
     * 带条件分页查询消息记录表列表
     *
     * @param params  消息记录表
     * @return PageVO
     */
    @Override
    public PageVO<MessageReceiver> findByPage(Map<String, String> params) {
        int pageNo = null == params.get("pageNo") ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = null == params.get("pageSize") ? 15 : Integer.parseInt(params.get("pageSize"));
        List<Object> args = new ArrayList<>();
        String sql = " SELECT t1.*,t2.title,t2.msg,t2.extra,t2.category "
                   + " FROM message_receiver t1 LEFT JOIN message t2 ON t2.id = t1.message_id "
                   + " WHERE 1=1 ";
        if (Objects.nonNull(UserSession.getUserSession()) && Objects.nonNull(UserSession.getUserSession().getUserId())) {
            sql += "AND t1.user_id = ? ";
            args.add(UserSession.getUserSession().getUserId());
        }
        sql += " ORDER BY t1.create_time DESC";
        return getPage(sql, pageNo, pageSize, args, new BeanPropertyRowMapper(MessageReceiver.class));
    }

    /**
     * 删除消息记录表
     *
     * @param id
     * @return
     */
    @Override
    public int deleteById(String id) {
        messageReceiverRepository.deleteById(id);
        return 1;
    }

    /**
     * 批量删除消息记录表
     *
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(String ids) {
        Stream.of(StringUtils.split(ids, ",")).forEach(messageReceiverRepository::deleteById);
        return 1;
    }

    /**
     * 根据userId查询未读信息数量
     *
     * @param userId
     * @return
     */
    @Override
    public int findUnreadCountByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM message_receiver WHERE user_id = ? AND status = 0";
        return jdbc.queryForObject(sql, new Object[]{userId}, int.class);
    }

    /**
     * 批量已读
     *
     * @param ids
     */
    @Override
    public void batchReadMessage(String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] idArray = StringUtils.split(ids, ",");
            String sql = " UPDATE message_receiver SET status = 1 , finish_date = ? WHERE id = ? ";
            List<Object[]> params = Stream.of(idArray).map(id -> new Object[]{CommonUtils.getStringDate(new Date()), id}).collect(Collectors.toList());
            jdbc.batchUpdate(sql, params);
            pushMessageForPC();
        }
    }

    /**
     * 全部已读
     */
    @Override
    public void allReadMessage() {
        if (Objects.nonNull(UserSession.getUserSession())) {
            String sql = "UPDATE message_receiver SET status = 1 , finish_date = ? WHERE user_id = ? ";
            jdbc.update(sql, CommonUtils.getStringDate(new Date()), UserSession.getUserSession().getUserId());
            pushMessageForPC();
        }
    }

    private void pushMessageForPC() {
        UserSession userSession = UserSession.getUserSession();
        if (Objects.nonNull(userSession) && Objects.nonNull(userSession.getUserId())) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("lastMessageCount", findUnreadCountByUserId(userSession.getUserId()));
            webSocketService.pushMessage(userSession.getUserId(), extra);
        }
    }
}