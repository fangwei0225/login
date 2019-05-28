package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.MessageService;
import com.taoding.mp.base.service.UserService;
import com.taoding.mp.util.JPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息 Controller
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
@RestController
@RequestMapping(value = "/server/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private JPushService jPushService;

    @Autowired
    private UserService userService;

    /**
     * 保存消息
     *
     * @param message
     * @return Message
     */
    @PostMapping(value = "/save")
    public ResponseVO<Message> save(@RequestBody Message message) {
        return new ResponseVO<>(messageService.save(message));
    }

    /**
     * 更新消息
     *
     * @param message
     * @return Message
     */
    @PostMapping(value = "/update")
    public ResponseVO<Message> update(@RequestBody Message message) {
        return new ResponseVO<>(messageService.update(message));
    }

    /**
     * 查询消息列表
     *
     * @return 消息List
     */
    @PostMapping(value = "/findByList")
    public ResponseVO<List<Message>> findByList() {
        return new ResponseVO<>(messageService.findByList());
    }

    /**
     * 查询消息信息
     *
     * @param id 消息Id
     * @return 消息
     */
    @GetMapping(value = "/findById")
    public ResponseVO<Message> findById(String id) {
        return new ResponseVO<>(messageService.findById(id));
    }

    /**
     * 带条件分页查询消息列表
     *
     * @param params 消息
     * @return 消息
     */
    @PostMapping("/findByPage")
    public ResponseVO<PageVO<Message>> findByPage(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(messageService.findByPage(params));
    }

    /**
     * 删除消息
     *
     * @param id
     * @return Message
     */
    @GetMapping(value = "/deleteById")
    public ResponseVO<Message> deleteById(String id) {
        messageService.deleteById(id);
        return new ResponseVO<>(null);
    }

    /**
     * 批量删除消息
     *
     * @param ids
     * @return Message
     */
    @GetMapping(value = "/deleteByIds")
    public ResponseVO<Message> deleteByIds(String ids) {
        messageService.deleteByIds(ids);
        return new ResponseVO<>(null);
    }





}