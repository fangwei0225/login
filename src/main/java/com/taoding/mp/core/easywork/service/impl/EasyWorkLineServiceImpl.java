package com.taoding.mp.core.easywork.service.impl;

import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.dao.UserRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.easywork.dao.MiddleBacklogRepository;
import com.taoding.mp.core.easywork.entity.MiddleBacklog;
import com.taoding.mp.core.easywork.service.EasyWorkLineService;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.FlowWorkFileRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.flow.vo.ViewDataVO;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.project.service.ProjectInfoService;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.dao.WorkRecordRepository;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.vo.WorkLineVO;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.CreateObjUtils;
import com.taoding.mp.util.RedisSync;
import com.taoding.mp.util.UpdateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: youngsapling
 * @date: 2019-05-14
 * @modifyTime:
 * @description:
 */
@Slf4j
@Service
public class EasyWorkLineServiceImpl implements EasyWorkLineService {
    @Autowired
    RedisSync redisSync;
    @Autowired
    ProjectInfoRepository projectInfoRepository;
    @Autowired
    FlowTreeService flowTreeService;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    WorkRecordRepository workRecordRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FlowWorkFileRepository flowWorkFileRepository;
    @Autowired
    MiddleBacklogRepository middleBacklogRepository;

    @Autowired
    ProjectInfoService projectInfoService;

    @Override
    public PreviewVO getByLevel(String projectId, Integer level, String flowTreeId) {
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        if (null == projectInfo) {
            log.error("查询办理进度时项目projectId[{}]不存在.", projectInfo);
            return new PreviewVO();
        }
        String type = "";
        // 拿到视图
        PreviewVO from = null;
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)) {
            //是主流程
            type = projectInfo.getType().toString();
            from = flowTreeService.viewByApp(type, level);
        } else if (Constants.FLOW_NODE_SUB_LEVEL.equals(level)) {
            //是子流程
            from = flowTreeService.projectSubTree(flowTreeId, projectInfo.getGrade());
        }
        if (null == from) {
            log.error("办理进度这里通过param1[{}] and level[{}]没有查询到节点模板.", type, level);
            return new PreviewVO();
        }

        List<ViewDataVO> dataMap = from.getDataMap();
        Map<String, FlowTree> flowTrees = new HashMap<>(dataMap.size());
        dataMap.forEach(e -> flowTrees.put(e.getKey(), e.getText()));
        List<Map<String, Object>> workLineList = new ArrayList<>(flowTrees.size());
        // 查询这个项目所有的已落库流水线节点
        List<WorkLine> workLines = workLineRepository.findByProjectIdAndIsDelete(projectId, 1);
        // 转换为map
        Map<String, WorkLine> didWorkLineMap = new HashMap<>(workLines.size());
        workLines.forEach(w -> didWorkLineMap.put(w.getFlowTreeId(), w));
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)) {
            //是主流程
            workLineList = converForMain(flowTrees, didWorkLineMap, workLineList);
        } else if (Constants.FLOW_NODE_SUB_LEVEL.equals(level)) {
            //是子流程
            workLineList = converForChild(flowTrees, didWorkLineMap, workLineList, projectInfo);
        }
        from.setWorkLineList(workLineList);
        return from;
    }

    /**
     * 将节点转换为主流程显示的节点.
     *
     * @param flowTrees
     * @param didWorkLineMap
     * @param workLineList
     * @return
     */
    private List converForMain(Map<String, FlowTree> flowTrees, Map<String, WorkLine> didWorkLineMap,
                               List<Map<String, Object>> workLineList) {
        List<Department> taoding = deptRepository.findAllByCorpIdOrderByNumDesc("taoding");
        Map<String, String> deptNameMap = new HashMap<>(taoding.size());
        taoding.stream().forEach(department -> deptNameMap.put(department.getId(), department.getName()));

        for (Map.Entry<String, FlowTree> entry : flowTrees.entrySet()) {
            FlowTree flowTree = entry.getValue();
            WorkLine workLine = didWorkLineMap.get(flowTree.getId());
            if (null == workLine) {
                //还没有flowTree对应的workLine
                workLine = new WorkLine();
                workLine.converFlowTreeToWorkLine(flowTree);
                workLine.setStatus(0);
            } else {
                //已经有flowTree对应的workLine
                workLine.converFlowTreeToWorkLine(flowTree);
            }
            WorkLineVO vo = new WorkLineVO();
            UpdateUtils.copyNonNullProperties(workLine, vo);
            //因为是主流程, 所以查询的是单位id
            vo.setDeptName(deptNameMap.get(vo.getUnitIds()));
            Map<String, Object> temp = new HashMap<>(2);
            temp.put("key", vo.getFlowTreeId());
            temp.put("object", vo);
            workLineList.add(temp);
        }
        return workLineList;
    }

    /**
     * 将节点转换为子流程显示的节点.
     *
     * @param flowTrees
     * @param didWorkLineMap
     * @param workLineList
     * @return
     */
    private List converForChild(Map<String, FlowTree> flowTrees, Map<String, WorkLine> didWorkLineMap,
                                List<Map<String, Object>> workLineList, ProjectInfo projectInfo) {
        List<Department> taoding = deptRepository.findAllByCorpIdOrderByNumDesc("taoding");
        Map<String, String> deptNameMap = new HashMap<>(taoding.size());
        taoding.stream().forEach(department -> deptNameMap.put(department.getId(), department.getName()));

        for (Map.Entry<String, FlowTree> entry : flowTrees.entrySet()) {
            FlowTree flowTree = entry.getValue();
            WorkLine workLine = didWorkLineMap.get(flowTree.getId());
            WorkLineVO vo = new WorkLineVO();
            vo.setGrade(null == flowTree.getGrade() ? 0 : flowTree.getGrade());
            if (null == workLine) {
                //还没有flowTree对应的workLine
                vo.converFlowTreeToWorkLine(flowTree);
                vo.setStatus(1);
                vo.setHasRemarkList(0);
                vo.setHasDataList(0);
            } else {
                //已经有flowTree对应的workLine
                vo.converFlowTreeToWorkLine(flowTree);
                UpdateUtils.copyNonNullProperties(workLine, vo);
                //判断有无 备注 或 申报材料
                setRemarkAndData(workLine.getId(), vo);
            }
            //判断有无节点备注
            vo.setHasFlowTreeRemark(StringUtils.isBlank(flowTree.getRemark()) ? 0 : 1);
            //设置办理单位名称.
            //因为是子流程, 所以查询的是deptIds
            if (Constants.TODO_JIEBAN.equals(flowTree.getType())) {
                //如果是特殊标识的科室, 那么去项目信息中查询.
                String deptId = projectInfo.getStreetOffice();
                String unitId = deptRepository.findById(deptId).orElse(new Department()).getParentId();
                String unitName = deptNameMap.get(unitId) == null ? "" : deptNameMap.get(unitId);
                String depteName = deptNameMap.get(deptId) == null ? "" : ">>" + deptNameMap.get(deptId);
                depteName = unitName + depteName;
                if (StringUtils.isBlank(depteName)) {
                    log.error("街办办理的depteName is null, backlogDeptId[{}]", deptId);
                }
                vo.setDeptName(depteName);
                vo.setDeptIds(deptId);
            } else {
                //不是特殊标识的, 正常走.
                String unitName = deptNameMap.get(vo.getUnitIds()) == null ? "" : deptNameMap.get(vo.getUnitIds());
                String depteName = deptNameMap.get(vo.getDeptIds()) == null ? "" : ">>" + deptNameMap.get(vo.getDeptIds());
                depteName = unitName + depteName;
                if (StringUtils.isBlank(depteName)) {
                    log.error("科室办理的depteName is null, backlogDeptId[{}]", vo.getDeptIds());
                }
                vo.setDeptName(depteName);
            }
            // 如果该项目未处理并且科室和登录用户的科室一样, 则允许处理.
            if (Constants.WORKLINE_RESULT_DOING.equals(vo.getStatus()) &&
                    vo.getDeptIds().equals(UserSession.getUserSession().getDeptId())) {
                vo.setPermission(Integer.valueOf(Constants.BUTTON_WRITE));
            } else {
                vo.setPermission(Integer.valueOf(Constants.BUTTON_READ));
            }
            //如果是菱形节点, 那么就不能操作.
            if (!flowTree.getGrade().equals(Constants.PROJECT_GRADE_NONE)) {
                vo.setPermission(Integer.valueOf(Constants.BUTTON_READ));
            }
            vo.setProjectId(projectInfo.getId());
            Map<String, Object> temp = new HashMap<>(2);
            temp.put("key", vo.getFlowTreeId());
            temp.put("object", vo);
            workLineList.add(temp);
        }
        return workLineList;
    }

    /**
     * 判断有无 备注列表 和 申报材料列表
     *
     * @param workLineId
     * @param vo
     */
    private void setRemarkAndData(String workLineId, WorkLineVO vo) {
        // 暂时先按这种方法一个一个查询, 因为workRecord表中没有projectId, 无法直接查询出来.
        Map<Integer, List<WorkRecord>> collect = workRecordRepository.findByWorkLineIdAndIsDelete(workLineId, Constants.STATUE_NORMAL)
                .stream().collect(Collectors.groupingBy(WorkRecord::getType));
        List<WorkRecord> dataList = collect.get(Integer.valueOf("1"));
        if (CollectionUtils.isNotEmpty(dataList)) {
            vo.setHasDataList(1);
        } else {
            vo.setHasDataList(0);
        }
        List<WorkRecord> remarkList = collect.get(Integer.valueOf("0"));
        if (CollectionUtils.isNotEmpty(remarkList)) {
            vo.setHasRemarkList(1);
        } else {
            //在workRecord中没有备注的前提下, 再判断一下这个节点在完成或跳过的时候, 有没有设置备注.
            if (StringUtils.isNotBlank(vo.getRemark())) {
                vo.setHasRemarkList(1);
            } else {
                vo.setHasRemarkList(0);
            }
        }
    }

    @Override
    public boolean completeWorkLine(WorkLine workLine) {
        if(StringUtils.isBlank(workLine.getId())){
            // 需要新建节点.
            workLine = CreateObjUtils.addBase(workLine);
        }else {
            // 因为在创建附件等信息的时候, 要挂在workLine节点上, 所以可能已经提前创建了.
            WorkLine database = workLineRepository.findByIdAndIsDelete(workLine.getId(), Constants.STATUE_NORMAL).orElse(new WorkLine());
            if (null != database && Constants.WORKLINE_RESULT_DOING.equals(database.getResult())) {
                // 是处理中的workLine
                BeanUtils.copyProperties(database, workLine);
            } else {
                // 这个节点已经处理过
                log.error("该节点已办理过.");
                return false;
            }
        }
        FlowTree flowTreeTemp = flowTreeRepository.findById(workLine.getFlowTreeId()).orElse(null);
        if (null == flowTreeTemp) {
            log.error("没查询到对应的flowTree[{}]", workLine.getFlowTreeId());
            return false;
        }
        FlowTree flowTree = new FlowTree();
        BeanUtils.copyProperties(flowTreeTemp, flowTree);
        // 如果是街办处理, 设置真实的处理街办.
        if (Constants.TODO_JIEBAN.equals(flowTree.getType())) {
            ProjectInfo projectInfo = projectInfoRepository.findById(workLine.getProjectId()).orElse(new ProjectInfo());
            String projectDeptIds = projectInfo.getStreetOffice();
            Department department = deptRepository.findById(projectDeptIds).orElse(new Department());
            flowTree.setUnitIds(department.getParentId());
            flowTree.setDeptIds(projectDeptIds);
        }
        // 转换
        workLine.converFlowTreeToWorkLine(flowTree);
        if (StringUtils.isBlank(workLine.getOperatorName())) {
            workLine.setOperatorName(UserSession.getUserSession().getName());
        }
        // 设置更新时间.
        workLine.setUpdateTime(CommonUtils.getStringDate(new Date()));
        workLine.setStatus(Constants.WORKLINE_STATUS_DID);
        workLineRepository.save(workLine);
        return true;
    }

    @Override
    public boolean toNext(String projectId, String flowTreeId) {
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        Asserts.notNull(projectInfo, "没有查询到项目信息: " + projectId);
        PreviewVO previewVO = flowTreeService.projectSubTree(flowTreeId, projectInfo.getGrade());
        List<String> otherFlowTreeList = new ArrayList<>(previewVO.getDataMap().size() >> 1);
        List<String> myDeptFlowTreeIds = new ArrayList<>(previewVO.getDataMap().size() >> 1);
        String deptTemp = null;
        // 拿到了该子流程的全部数据
        for (ViewDataVO viewDataVO : previewVO.getDataMap()) {
            // 如果是菱形节点, 跳过.
            if (!Constants.PROJECT_GRADE_NONE.equals(viewDataVO.getText().getGrade())) {
                continue;
            }
            // 将子流程中人为添加的第一个节点过滤掉, 就是一级节点
            if(0 == viewDataVO.getText().getLevel()){
                continue;
            }
            // 如果是街办处理, 设置真实处理单位
            if (Constants.TODO_JIEBAN.equals(viewDataVO.getText().getType())) {
                deptTemp = projectInfo.getStreetOffice();
            } else {
                deptTemp = viewDataVO.getText().getDeptIds();
            }
            // 将科室分离 我的科室的进我的科室的, 别的进别的, 便于后续判断数量.
            if (deptTemp.equals(UserSession.getUserSession().getDeptId())) {
                myDeptFlowTreeIds.add(viewDataVO.getText().getId());
            } else {
                otherFlowTreeList.add(viewDataVO.getText().getId());
            }
        }
        List<WorkLine> databaseWorkLineList = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectId,
                myDeptFlowTreeIds, Constants.STATUE_NORMAL);
        if (myDeptFlowTreeIds.size() != databaseWorkLineList.size()) {
            //先通过数量判断
            log.info("用户点击提交下一步, 应完成[{}]个节点, 实际完成[{}]个.user[{}]", myDeptFlowTreeIds.size(),
                    databaseWorkLineList.size(), UserSession.getUserSession().getName());
            return false;
        }else {
            // 再遍历实际的判断, 因为在创建附件的时候, 附件是挂在workLine上的, 此时workLine如果没有操作,
            // 那么是创建了一个无结果的对象
            for (WorkLine workLine : databaseWorkLineList) {
                if (workLine.getStatus() == Constants.WORKLINE_STATUS_DOING){
                    log.info("项目[{}]在环节id[{}]中的节点name:[{}], id:[{}]还没有处理.",projectInfo.getName(), flowTreeId,
                            workLine.getName(), workLine.getId());
                    return false;
                }
            }
        }
        // 该科室的完成. 将该科室从待办中间表状态修改.
        middleBacklogForSelfOK(projectId, flowTreeId, UserSession.getUserSession().getDeptId());
        // 继续判断剩余的是否都完成.
        List<WorkLine> otherWorkLineList = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectId,
                otherFlowTreeList, Constants.STATUE_NORMAL);
        if (otherFlowTreeList.size() != otherWorkLineList.size()) {
            // 其余科室有未完成的, 返回.
            log.info("用户[{}]操作项目[{}]提交下一步, 自己科室的完成了, 其余科室应完成[{}]个节点, 实际完成[{}]个.",
                    UserSession.getUserSession().getName(), projectInfo.getName(), otherFlowTreeList.size(), otherWorkLineList.size());
            return false;
        }else {
            for (WorkLine workLine : otherWorkLineList) {
                if (workLine.getStatus() == Constants.WORKLINE_STATUS_DOING){
                    log.info("项目[{}]在环节id[{}]中的节点name:[{}], id:[{}]还没有处理.",projectInfo.getName(), flowTreeId,
                            workLine.getName(), workLine.getId());
                    return false;
                }
            }
        }
        // 其余科室也完成了, 那么 更新主节点
        mainWorkLineForAllOK(projectId, flowTreeId);
        // 下一个主节点.
        addNextWorkLineAndMiddle(projectInfo, flowTreeId);
        return true;
    }

    /**
     *
     * @param projectId
     * @param flowTreeId
     * @param deptId
     */
    private void middleBacklogForSelfOK(String projectId, String flowTreeId, String deptId) {
        MiddleBacklog middleBacklog = middleBacklogRepository.findByProjectIdAndFlowTreeIdAndDeptIdAndIsDelete(projectId,
                flowTreeId, deptId, Constants.STATUE_NORMAL);
        middleBacklog.setStatus(Constants.WORKLINE_STATUS_DID);
        middleBacklog.setUpdateTime(CommonUtils.getStringDate(new Date()));
        middleBacklogRepository.save(middleBacklog);
    }

    /**
     * 更新workLine表中 对应的主流程节点状态 为已办.
     *
     * @param projectId
     * @param flowTreeId
     */
    private void mainWorkLineForAllOK(String projectId, String flowTreeId) {
        WorkLine mainWorkLine = workLineRepository.findByProjectIdAndFlowTreeIdAndIsDelete(projectId, flowTreeId, Constants.STATUE_NORMAL);
        mainWorkLine.setStatus(Constants.WORKLINE_STATUS_DID);
        mainWorkLine.setResult(Constants.WORKLINE_RESULT_DID);
        mainWorkLine.setUpdateTime(CommonUtils.getStringDate(new Date()));
        workLineRepository.save(mainWorkLine);
    }

    /**
     * 添加输入一级主节点的后续主节点能否添加.
     *
     * @param projectInfo
     * @param flowTreeId
     */
    private void addNextWorkLineAndMiddle(ProjectInfo projectInfo, String flowTreeId) {
        // 获得下一个主流程节点的头对象.可能是多个
        List<FlowTree> flowTreeList = flowTreeService.selectNextMainNodeById(flowTreeId);
        if (null == flowTreeList || flowTreeList.isEmpty()) {
            //没有下一个主流程节点了, 说明流程完了.
            log.info("projectId[{}]流程走完了.", projectInfo.getId());
            projectInfoService.updateResult(projectInfo.getId(), Constants.PROJECT_RESULT_OVER);
            return;
        }
        // 判断主节点是否可以添加.
        for (FlowTree flowTree : flowTreeList) {
            boolean did = isAllDidByFlowTree(projectInfo.getId(), flowTree, "status");
            if (did) {
                //可以添加, 将主节点创建在workLine中,
                WorkLine workLine = CreateObjUtils.create(WorkLine.class);
                workLine.converFlowTreeToWorkLine(flowTree);
                workLine.setStatus(Constants.WORKLINE_STATUS_DOING);
                workLine.setResult(Constants.WORKLINE_RESULT_DOING);
                workLineRepository.save(workLine);
                //将主节点的子流程对应到的科室创建到MiddleBacklog表中
                addDeptToMiddleBacklog(projectInfo, flowTree.getId());
            }
        }
    }

    /**
     * 判断这个一级节点是否满足添加条件
     *
     * @param projectId
     * @param flowTree
     * @param condition
     * @return
     */
    public boolean isAllDidByFlowTree(String projectId, FlowTree flowTree, String condition) {
        List<String> parentIds = CommonUtils.StringToList(flowTree.getParentId(), ",");
        List<WorkLine> workLines = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectId, parentIds, Constants.STATUE_NORMAL);
        if (CollectionUtils.isEmpty(workLines)) {
            log.error("projectId[{}] + flowTreeId[{}] 查询结果为null", projectId, flowTree.getParentId());
            return false;
        }
        if (null != parentIds && null != workLines && parentIds.size() != workLines.size()) {
            //记录中的数量 和 parentIds 的数量不相等, 可能是因为有的前置节点还没创建.
            log.info("parentIds is [{}], but had workLines is [{}]", parentIds.size(), workLines.size());
            return false;
        }
        for (WorkLine workLine : workLines) {
            if (Constants.WORKLINE_STATUS_DOING == workLine.getStatus()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将主节点的子流程对应到的科室创建到MiddleBacklog表中
     *
     * @param projectInfo
     * @param flowTreeId
     */
    private void addDeptToMiddleBacklog(ProjectInfo projectInfo, String flowTreeId) {
        PreviewVO previewVO = flowTreeService.projectSubTree(flowTreeId, projectInfo.getGrade());
        // 获取模板中应该有待办的科室的id, 如果是街办处理则使用项目中的数据.
        List<String> deptIdList = previewVO.getDataMap().stream().map(viewDataVO -> {
            if (Constants.TODO_JIEBAN.equals(viewDataVO.getText().getType())) {
                return projectInfo.getStreetOffice();
            } else {
                return viewDataVO.getText().getDeptIds();
            }
        }).filter(e -> StringUtils.isNotBlank(e)).distinct().collect(Collectors.toList());
        List<MiddleBacklog> middleBacklogList = new ArrayList<>(deptIdList.size());
        for (String deptId : deptIdList) {
            MiddleBacklog middleBacklog = CreateObjUtils.create(MiddleBacklog.class);
            middleBacklog.setProjectId(projectInfo.getId());
            middleBacklog.setFlowTreeId(flowTreeId);
            middleBacklog.setDeptId(deptId);
            middleBacklog.setStatus(Constants.WORKLINE_STATUS_DOING);
            middleBacklogList.add(middleBacklog);
        }
        middleBacklogRepository.saveAll(middleBacklogList);
    }

    @Override
    public boolean initWorkLine(String projectId) {
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        if (null == projectInfo) {
            log.error("initWorkLine()error: projectId[{}]查不到数据.", projectId);
            return false;
        }
        if(projectInfo.getResult() != Constants.PROJECT_RESULT_NONE){
            log.error("projectId[{}]已经有激活状态的工作流了.", projectId);
            return false;
        }
        //添加锁, 防止用户重复点击造成数据错误.
        String sync = redisSync.getSync(projectId, 120, TimeUnit.SECONDS);
        if(StringUtils.isBlank(sync)){
            log.error("获取锁失败.");
            return false;
        }
        try {
            Integer type = projectInfo.getType();
            FlowTree flowTreeBase = flowTreeService.selectTopId(type, "0", "0");
            if (flowTreeBase == null) {
                log.error("initWorkLine()error: type[{}]查不到数据.", type);
                return false;
            }
            addDeptToMiddleBacklog(projectInfo, flowTreeBase.getId());
            projectInfoService.updateResult(projectId, Constants.PROJECT_RESULT_DOING);
            return true;
        }finally {
            redisSync.removeSync(projectId, sync);
        }
    }
}
