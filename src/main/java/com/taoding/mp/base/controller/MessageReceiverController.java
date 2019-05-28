package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.MessageReceiverService;
import com.taoding.mp.util.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 消息记录表 Controller
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@RestController
@RequestMapping(value = "/server/messageReceiver")
public class MessageReceiverController {

    @Autowired
    private MessageReceiverService messageReceiverService;

    @Autowired
    private WebSocketService webSocketService;



    /**
     * 保存消息记录表
     *
     * @param messageReceiver
     * @return MessageReceiver
     */
    @PostMapping(value = "/save")
    public ResponseVO<MessageReceiver> save(@RequestBody MessageReceiver messageReceiver) {
        return new ResponseVO<>(messageReceiverService.save(messageReceiver));
    }

    /**
     * 更新消息记录表
     *
     * @param messageReceiver
     * @return MessageReceiver
     */
    @PostMapping(value = "/update")
    public ResponseVO<MessageReceiver> update(@RequestBody MessageReceiver messageReceiver) {
        return new ResponseVO<>(messageReceiverService.update(messageReceiver));
    }

    /**
     * 批量已读
     *
     * @param ids
     * @return
     */
    @GetMapping(value = "/batchRead")
    public ResponseVO<Object> batchUpdateReadMessage(String ids) {
        messageReceiverService.batchReadMessage(ids);
        return new ResponseVO<>(null);
    }

    /**
     * 全部已读
     *
     * @return
     */
    @GetMapping(value = "/allRead")
    public ResponseVO<Object> allUpdateReadMessage() {
        messageReceiverService.allReadMessage();
        return new ResponseVO<>(null);
    }


    /**
     * 查询消息记录表列表
     *
     * @return 消息记录表List
     */
    @PostMapping(value = "/findByList")
    public ResponseVO<List<MessageReceiver>> findByList() {
        return new ResponseVO<>(messageReceiverService.findByList());
    }

    /**
     * 查询消息记录表信息
     *
     * @param id 消息记录表Id
     * @return 消息记录表
     */
    @GetMapping(value = "/findById")
    public ResponseVO<MessageReceiver> findById(String id) {
        return new ResponseVO<>(messageReceiverService.findById(id));
    }

    /**
     * 带条件分页查询消息记录表列表
     *
     * @param params 消息记录表
     * @return 消息记录表
     */
    @PostMapping("/findByPage")
    public ResponseVO<PageVO<MessageReceiver>> findByPage(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(messageReceiverService.findByPage(params));
    }

    /**
     * 删除消息记录表
     *
     * @param id
     * @return MessageReceiver
     */
    @GetMapping(value = "/deleteById")
    public ResponseVO<MessageReceiver> deleteById(String id) {
        messageReceiverService.deleteById(id);
        return new ResponseVO<>(null);
    }

    /**
     * 批量删除消息记录表
     *
     * @param ids
     * @return MessageReceiver
     */
    @GetMapping(value = "/deleteByIds")
    public ResponseVO<MessageReceiver> deleteByIds(String ids) {
        messageReceiverService.deleteByIds(ids);
        return new ResponseVO<>(null);
    }

    /**
     * 根据userId查询未读信息数量
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/findUnreadCountByUserId")
    public ResponseVO<Integer> findUnreadCountByUserId(String userId) {
        return new ResponseVO<>(messageReceiverService.findUnreadCountByUserId(Objects.isNull(userId) ? UserSession.getUserSession().getUserId() : userId));
    }






}