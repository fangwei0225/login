package com.taoding.mp.core.work.eventbus.eventlistener;

import com.alibaba.fastjson.JSONObject;
import com.baidubce.services.cdn.model.JsonObject;
import com.google.common.eventbus.Subscribe;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.dao.UserRepository;
import com.taoding.mp.base.entity.Message;
import com.taoding.mp.base.entity.MessageReceiver;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.base.model.MessageDTO;
import com.taoding.mp.base.service.MessageService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.work.eventbus.event.BacklogEvent;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.JPushService;
import com.taoding.mp.util.RedisSync;
import com.taoding.mp.util.WebSocketService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author: youngsapling
 * @date: 2019-05-10
 * @modifyTime:
 * @description: 流程节点有更新的监听器.
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class BacklogEventListener {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JPushService jPushService;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    RedisSync redisSync;
    @Autowired
    MessageService messageService;

    @Subscribe
    public void onEvent(BacklogEvent event) {
        String backlogDeptId = event.getBacklogDeptId();
        if (StringUtils.isNotBlank(backlogDeptId)) {
            sendBacklog(event);
        }
    }

    private void sendBacklog(BacklogEvent event) {
        String projectId = event.getProjectId();
        String projectName = StringUtils.isBlank(event.getProjectName()) ? "" : event.getProjectName();
        String backlogDeptId = event.getBacklogDeptId();
        Integer isGroup = event.getProjectIsGroup();
        String mainWorkLineName = event.getMainWorkLineName();
        String title = Constants.BACKLOG_TITLE;
        String groups = event.getGroups();
        Integer status = event.getStatus();
        String workLineName = event.getWorkLineName();
        String message = projectName + "-" + mainWorkLineName;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectId", projectId);
        jsonObject.put("projectName", projectName);
        jsonObject.put("groups", groups);
        jsonObject.put("status", status);
        jsonObject.put("isGroup", isGroup);
        jsonObject.put("workLineName", workLineName);
        jsonObject.put("type", Constants.MSG_TO_BACKLOG);
        jsonObject.put("createTime", CommonUtils.getStringDate(new Date()));
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(message);
        messageDTO.setTitle(title);
        messageDTO.setExtra(jsonObject);
        List<User> userList = userRepository.findByDeptId(backlogDeptId);
        messageDTO.setReceivers(userList);
        if (CollectionUtils.isNotEmpty(userList) && easySend(projectId, groups, Constants.MSG_TO_BACKLOG)) {
            Message messageReceiver = messageService.buildAndSaveMessage(messageDTO);
            List<MessageReceiver> messageReceivers = messageService.buildAndMessageReceiver(messageReceiver, messageDTO);
            jPushService.pushMessage(messageReceiver, messageDTO, messageReceivers);
            webSocketService.pushMessage(messageReceiver, messageDTO, messageReceivers);
        }else {
            log.info("推送{}信息时, deptId[{}]下没有人员.", Constants.MSG_TO_BACKLOG, backlogDeptId);
        }
    }

    /**
     * 限制太密集的消息轰炸.
     *
     * @param projectId
     * @param groups
     * @param type
     */
    private boolean easySend(String projectId, String groups, String type) {
        String key = "message:" + projectId + ":" + groups + ":" + type;
        String sync = redisSync.getSync(key, 10, TimeUnit.SECONDS);
        return StringUtils.isNotBlank(sync);
    }

}
