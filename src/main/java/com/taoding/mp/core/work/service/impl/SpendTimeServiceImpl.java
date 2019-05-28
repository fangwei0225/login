package com.taoding.mp.core.work.service.impl;

import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.work.dao.SpendTimeRepository;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.entity.SpendTime;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.service.SpendTimeService;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.CreateObjUtils;
import com.taoding.mp.util.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: youngsapling
 * @date: 2019-05-16
 * @modifyTime:
 * @description:
 */
@Slf4j
@Service
public class SpendTimeServiceImpl implements SpendTimeService {
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    SpendTimeRepository spendTimeRepository;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    ProjectInfoRepository projectInfoRepository;
    @Autowired
    FlowTreeService flowTreeService;

    @Override
    public void addMainWorkLine(WorkLine childWorkLine) {
        // 先找到对应的一级节点的flowTreeId
        FlowTree flowTree = flowTreeRepository.findById(childWorkLine.getTreeTopId()).orElse(null);
        if (null == flowTree) {
            log.error("创建耗时记录, 通过FlowTreeId({})没有查询到flowTree对象.", childWorkLine.getTreeTopId());
            return;
        }
        // 找到该项目对应的workLineId
        WorkLine mainWorkLine = workLineRepository.findByProjectIdAndFlowTreeIdAndIsDelete(childWorkLine.getProjectId(),
                flowTree.getId(), Constants.STATUE_NORMAL);
        if (null == mainWorkLine) {
            log.error("通过ProjectId({}) and FlowTreeId({})没有查询到workLine对象.",
                    childWorkLine.getProjectId(), childWorkLine.getTreeTopId());
            return;
        }
        // 找到对应的耗时记录
        SpendTime spendTime = spendTimeRepository.findByWorkLineIdAndIsDelete(mainWorkLine.getId(),
                Constants.STATUE_NORMAL).orElse(null);
        if (null != spendTime) {
            // 有记录了就返回.
            return;
        }
        // 没记录, 创建一个.
        spendTime = CreateObjUtils.create(SpendTime.class);
        spendTime.setProjectId(mainWorkLine.getProjectId());
        spendTime.setWorkLineId(mainWorkLine.getId());
        spendTime.setUpdateTime("");
        spendTime.setSpendLimit(flowTree.getDealTime());
        spendTime.setFlowModelId(mainWorkLine.getFlowModelId());
        spendTimeRepository.save(spendTime);
    }

    @Override
    public void update(WorkLine mainWorkLine) {
        SpendTime spendTime = spendTimeRepository.findByWorkLineIdAndIsDelete(mainWorkLine.getId(),
                Constants.STATUE_NORMAL).orElse(null);
        if (null == spendTime) {
            log.error("SpendTime表中通过workLineId[{}]没有查询到对应的数据.", mainWorkLine.getId());
            return;
        }
        SpendTime spendTemp = calculate(mainWorkLine);
        spendTemp.setUpdateTime(CommonUtils.getStringDate(new Date()));
        spendTimeRepository.save(spendTemp);
    }


    @Override
    public SpendTime calculate(WorkLine mainWorkLine) {
        Assert.notNull(mainWorkLine.getId(), "workLineId不能为null");
        Assert.notNull(mainWorkLine.getProjectId(), "projectId不能为null");
        SpendTime mainSpendTime = spendTimeRepository.findByWorkLineIdAndIsDelete(mainWorkLine.getId(),
                Constants.STATUE_NORMAL).orElse(null);

        if (null == mainSpendTime) {
            //没有记录的情况, 说明子流程节点还没有点击的
            SpendTime spendTime = new SpendTime();
            spendTime.setOverdue(Constants.NOT_OVERDUE);
            spendTime.setSpend(0);
            return spendTime;
        }
        ProjectInfo projectInfo = projectInfoRepository.findById(mainWorkLine.getProjectId()).orElse(null);
        if (null == projectInfo) {
            log.error("projectInfo[{}]没有查询到对象.", projectInfo.getId());
            SpendTime spendTime = new SpendTime();
            spendTime.setOverdue(Constants.NOT_OVERDUE);
            spendTime.setSpend(0);
            return spendTime;
        }
        //有记录的情况
        return todo(mainSpendTime, mainWorkLine, projectInfo);
    }

    private SpendTime todo(SpendTime spendTime, WorkLine mainWorkLine, ProjectInfo projectInfo){
        // 避免jpa造成异常
        SpendTime mainSpendTime = new SpendTime();
        BeanUtils.copyProperties(spendTime, mainSpendTime);
        Date current = new Date();
        Date mainUpdateTime = StringUtils.isBlank(mainSpendTime.getUpdateTime()) ? current :
                CommonUtils.StringToDate(mainSpendTime.getUpdateTime(), YYYY_MM_DD_HH_MM_SS);
        Date mainCreateTime = CommonUtils.StringToDate(mainSpendTime.getCreateTime(), YYYY_MM_DD_HH_MM_SS);
        // 主节点的粗略用时 --> 开始计算时间了
        Integer mainSpend = DateTools.getWorkDayBetween(mainCreateTime, mainUpdateTime);
        // 计算对应的子流程中特殊节点的用时
        if(StringUtils.isBlank(mainWorkLine.getFlowTreeId())){
            WorkLine workLine = workLineRepository.findByIdAndIsDelete(mainWorkLine.getId(), Constants.STATUE_NORMAL).orElse(new WorkLine());
            mainWorkLine.setFlowTreeId(workLine.getFlowTreeId());
        }
        List<FlowTree> flowTreeList = flowTreeService.listByMainId(mainWorkLine.getFlowTreeId());
        // 过滤掉不是局外运转的节点(outTime == 0)
        List<String> flowTreeIdList = flowTreeList.stream().filter(flowTree -> null != flowTree.getOutTime() &&
                flowTree.getOutTime() > 0).map(FlowTree::getId).collect(Collectors.toList());
        List<WorkLine> childWorkLines = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectInfo.getId(),
                flowTreeIdList, Constants.STATUE_NORMAL);
        Integer childSpend = 0;
        Date childUpdateTime, childCreateTime;
        for (WorkLine workLine : childWorkLines) {
            //子节点已办, 使用更新时间, 否则使用当前时间计算.
            if (Constants.WORKLINE_STATUS_DID.equals(workLine.getStatus())) {
                childUpdateTime = CommonUtils.StringToDate(workLine.getUpdateTime(), YYYY_MM_DD_HH_MM_SS);
            } else {
                childUpdateTime = current;
            }
            childCreateTime = CommonUtils.StringToDate(workLine.getCreateTime(), YYYY_MM_DD_HH_MM_SS);
            // 子节点用时
            childSpend += DateTools.getWorkDayBetween(childCreateTime, childUpdateTime);
        }
        Integer mainSpendLog = mainSpend;
        mainSpend = mainSpend - childSpend;
        if (mainSpend <= 0) {
            log.error("主节点[{}]时间计算错误, mainSpend[{}], childSpend[{}]", mainWorkLine.getId(), mainSpendLog, childSpend);
            mainSpend = 1;
        }
        // 期限大于等于主节点实际使用天数, 就没有逾期.
        Integer overdue = "0".equals(mainSpendTime.getSpendLimit().toString()) ? Constants.NOT_OVERDUE : mainSpendTime.getSpendLimit() >= mainSpend ? Constants.NOT_OVERDUE : Constants.IS_OVERDUE;
        mainSpendTime.setOverdue(overdue);
        mainSpendTime.setSpend(mainSpend);
        return mainSpendTime;
    }

    @Override
    public List<SpendTime> findByProjectIdAndIsDelete(String projectId, Integer isDelete) {
        return spendTimeRepository.findByProjectIdAndIsDelete(projectId, isDelete);
    }
}
