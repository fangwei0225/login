package com.taoding.mp.core.work.eventbus.eventlistener;

import com.alibaba.fastjson.JSONObject;
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
import com.taoding.mp.core.work.eventbus.event.BacklogEvent;
import com.taoding.mp.core.work.eventbus.event.ProjectEvent;
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
public class ProjectEventListener {
    @Autowired
    UserRepository userRepository;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    RedisSync redisSync;
    @Autowired
    JPushService jPushService;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    MessageService messageService;

    @Subscribe
    public void onEvent(ProjectEvent event) {
        String projectDeptId = event.getProjectDeptId();
        if (StringUtils.isNotBlank(projectDeptId)) {
            Arrays.asList(projectDeptId.split(Constants.SPLIT)).forEach(deptId -> sendProject(event, deptId));
        }
    }

    private void sendProject(ProjectEvent event, String deptId) {
        String projectId = event.getProjectId();
        String projectName = StringUtils.isBlank(event.getProjectName()) ? "" : event.getProjectName();
        Integer projectStatus = event.getProjectStatus();
        String mainWorkLineName = event.getMainWorkLineName();
        Integer projectIsGroup = event.getProjectIsGroup();
        String projectGroupId = event.getProjectGroupId();
        String title = Constants.PROJECT_TITLE;
        String message = projectName + "-" + mainWorkLineName;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectId", projectId);
        jsonObject.put("status", projectStatus);
        jsonObject.put("type", Constants.MSG_TO_PROJECT);
        jsonObject.put("isGroup", projectIsGroup);
        jsonObject.put("projectGroupId", projectGroupId);
        jsonObject.put("createTime", CommonUtils.getStringDate(new Date()));
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(message);
        messageDTO.setTitle(title);
        messageDTO.setExtra(jsonObject);
        List<User> userList = userRepository.findByDeptId(deptId);
        messageDTO.setReceivers(userList);
        if (CollectionUtils.isNotEmpty(userList) && easySend(projectId, deptId, Constants.MSG_TO_PROJECT)) {
            Message messageReceiver = messageService.buildAndSaveMessage(messageDTO);
            List<MessageReceiver> messageReceivers = messageService.buildAndMessageReceiver(messageReceiver, messageDTO);
            jPushService.pushMessage(messageReceiver, messageDTO, messageReceivers);
            webSocketService.pushMessage(messageReceiver, messageDTO, messageReceivers);
        }else {
            log.info("推送{}信息时, deptId[{}]下没有人员.", Constants.MSG_TO_PROJECT, deptId);
        }
    }

    /**
     * 限制太密集的消息轰炸.
     *
     * @param projectId
     * @param deptId
     * @param type
     */
    private boolean easySend(String projectId, String deptId, String type) {
        String key = "message:" + projectId + ":" + deptId + ":" + type;
        String sync = redisSync.getSync(key, 10, TimeUnit.SECONDS);
        return StringUtils.isNotBlank(sync);
    }

}
