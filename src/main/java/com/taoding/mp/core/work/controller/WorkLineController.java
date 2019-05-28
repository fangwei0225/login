package com.taoding.mp.core.work.controller;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.service.PlanService;
import com.taoding.mp.core.work.service.WorkLineService;
import com.taoding.mp.core.work.vo.BacklogVO;
import com.taoding.mp.core.work.vo.PlanVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: youngsapling
 * @date: 2019-04-16
 * @modifyTime:
 * @description:
 */
@RestController
@Slf4j
@RequestMapping("/server/workLine")
public class WorkLineController {
    @Autowired
    WorkLineService workLineService;
    @Autowired
    PlanService planService;

    /**
     * [初始化项目流水线]
     * @param workLine
     * @return
     */
    @PostMapping("/start")
    public ResponseVO start(@RequestBody WorkLine workLine) {
        String projectId = workLine.getProjectId();
        boolean b = workLineService.initWorkLine(projectId);
        return ResponseBuilder.success(b);
    }

    /**
     * [待办详情]
     */
    @PostMapping("/details")
    public ResponseVO details(@RequestBody Map<String, String> map) {
        List<WorkLine> byProjectIdAndDeptId = workLineService.getByProjectIdAndDeptId(map);
        Map<String, Object> stringObjectMap = negotiateButton(byProjectIdAndDeptId);
        return ResponseBuilder.success(stringObjectMap);
    }

    /**
     * [用户点击完成跳过]
     */
    @PostMapping("/complete")
    public ResponseVO completeWorkLine(@RequestBody WorkLine workLine) {
        boolean b = workLineService.completeWorkLine(workLine);
        return ResponseBuilder.success(b);
    }

    /**
     * [提交下一步]
     *
     * @param workLine
     * @return
     */
    @PostMapping("/toNext")
    public ResponseVO toNext(@RequestBody WorkLine workLine) {
        String projectId = workLine.getProjectId();
        String workLineId = workLine.getId();
        if (StringUtils.isAnyBlank(projectId, workLineId)) {
            log.error("用户[{}]操作 toNext方法, 参数有null", UserSession.getUserSession().getUserId());
        }
        workLineService.toNext(projectId, workLineId);
        return ResponseBuilder.success(true);
    }

    /**
     * [办理进度]
     *
     * @param map
     * @return
     */
    @PostMapping("/plan")
    public ResponseVO<PreviewVO> plan(@RequestBody Map<String, String> map) {
        String projectId = map.get("projectId");
        Integer level = Integer.valueOf(map.get("level"));
        String flowTreeId = map.get("flowTreeId");
        PreviewVO plan = workLineService.getPlan(projectId, level, flowTreeId);
        plan.setDataMap(null);
        return ResponseBuilder.success(plan);
    }

    /**
     * [流程说明(点击子节点后)]
     */
    @PostMapping("/getPlan")
    public ResponseVO<PlanVO> getPlan(@RequestBody Map<String, String> map) {
        String workLineId = map.get("workLineId");
        String flowTreeId = map.get("flowTreeId");
        PlanVO planVO = planService.get(flowTreeId, workLineId);
        return ResponseBuilder.success(planVO);
    }

    /**
     * [我的待办/已办]
     *
     * @param map
     * @return
     */
    @PostMapping("/backlog")
    public ResponseVO<PageVO<BacklogVO>> backlog(@RequestBody Map<String, String> map) {
        PageVO<BacklogVO> baseInfoFromDeptId = workLineService.getBaseInfoFromDeptId(map);
        return ResponseBuilder.success(baseInfoFromDeptId);
    }

    /**
     * [子流程退回]
     *
     * @param map
     * @return
     */
    @PostMapping("/reset")
    public ResponseVO<Boolean> reset(@RequestBody Map<String, String> map) {
        String projectId = map.get("projectId");
        Assert.notNull(projectId, "projectId不能为null.");
        String treeTopId = map.get("treeTopId");
        Assert.notNull(treeTopId, "treeTopId不能为null.");
        boolean result = workLineService.resetChildWorkLine(projectId, treeTopId);
        return ResponseBuilder.success(result);
    }

    /**
     * button 1(只能看) 2(可以修改)
     *
     * @param workLines
     * @return
     */
    private Map<String, Object> negotiateButton(List<WorkLine> workLines) {
        Map<String, Object> result = new HashMap<>(2);
        String flag = UserSession.getUserSession().getFlag();
        if (Constants.USER_FLAG_STAFF.equals(flag)) {
            if(CollectionUtils.isEmpty(workLines)){
                result.put("button", Constants.BUTTON_READ);
                result.put("list", workLines);
            }else {
                Integer status = workLines.get(0).getStatus();
                String buttonStatus = status.equals(Constants.WORKLINE_STATUS_DID) ? Constants.BUTTON_READ : Constants.BUTTON_WRITE;
                result.put("button", buttonStatus);
                result.put("list", workLines);
            }
        } else {
            //是区领导 或 部门领导
            result.put("button", Constants.BUTTON_READ);
            result.put("list", workLines);
        }
        result.put("username", UserSession.getUserSession().getName());
        return result;
    }
}
