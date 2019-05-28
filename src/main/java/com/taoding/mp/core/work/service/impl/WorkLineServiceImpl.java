package com.taoding.mp.core.work.service.impl;

import com.google.common.eventbus.EventBus;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.CustomHttpStatus;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.project.service.ProjectInfoService;
import com.taoding.mp.core.work.dao.WorkLineDAO;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.eventbus.event.BacklogEvent;
import com.taoding.mp.core.work.eventbus.event.ProjectEvent;
import com.taoding.mp.core.work.service.PlanService;
import com.taoding.mp.core.work.service.SpendTimeService;
import com.taoding.mp.core.work.service.WorkLineService;
import com.taoding.mp.core.work.service.WorkRecordService;
import com.taoding.mp.core.work.vo.BacklogVO;
import com.taoding.mp.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description:
 */
@Slf4j
@Service
public class WorkLineServiceImpl implements WorkLineService {
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    WorkLineDAO workLineDao;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProjectInfoRepository projectInfoRepository;
    @Autowired
    RedisSync redisSync;
    @Autowired
    PlanService planService;
    @Autowired
    FlowTreeService flowTreeService;
    @Autowired
    WorkRecordService workRecordService;
    @Autowired
    ProjectInfoService projectInfoService;
    @Autowired
    SpendTimeService spendTimeService;
    @Autowired
    EventBus eventBus;

    @Override
    public boolean initWorkLine(String projectId) {
        List<WorkLine> dataBaseWorkLine = workLineRepository.findByProjectIdAndIsDelete(projectId, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(dataBaseWorkLine)) {
            log.error("projectId[{}]已经有激活状态的工作流了.", projectId);
            return false;
        }
        //添加锁, 防止用户重复点击造成数据错误.
        String sync = redisSync.getSync(projectId, 120, TimeUnit.SECONDS);
        if (StringUtils.isBlank(sync)) {
            log.error("获取锁失败.");
            return false;
        }
        try {
            ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
            if (null == projectInfo) {
                log.error("initWorkLine()error: projectId[{}]查不到数据.", projectId);
                return false;
            }
            if(1 == projectInfo.getIsGroup()){
                // 说明这个项目是打包项目, 返回
                log.error("initWorkLine()error: projectId[{}]在将打包项目的壳子开启流程.", projectId);
                return false;
            }
            Integer type = projectInfo.getType();
            FlowTree flowTreeBase = flowTreeService.selectTopId(type, "0", "0");
            if (flowTreeBase == null) {
                log.error("initWorkLine()error: type[{}]查不到数据.", type);
                return false;
            }
            boolean startWorkLine = startWorkLine(flowTreeBase, projectInfo, type);
            projectInfoService.updateResult(projectId, Constants.PROJECT_RESULT_DOING);
            return startWorkLine;
        } finally {
            redisSync.removeSync(projectId, sync);
        }
    }

    /**
     * 通过主流程节点来开启, 这个独立出来, 在流转过程中可以复用.
     *
     * @param flowTreeBase
     * @param projectId
     * @param type
     * @return
     */
    @Override
    public boolean startWorkLine(FlowTree flowTreeBase, ProjectInfo projectInfo, Integer type) {
        List<WorkLine> waitWorkLine = new ArrayList<>();
        WorkLine workLine = CreateObjUtils.create(WorkLine.class);
        workLine.converFlowTreeToWorkLine(flowTreeBase);
        // 判断这个项目中的这个节点是否已存在.若存在就不能添加了.
        WorkLine isExistWorkLine = workLineRepository.findByProjectIdAndFlowTreeIdAndIsDelete(projectInfo.getId(), flowTreeBase.getId(), 1);
        if (null != isExistWorkLine) {
            log.error("ProjectId[{}], FlowTreeId[{}]已存在.", projectInfo.getId(), flowTreeBase.getId());
            return false;
        }
        waitWorkLine.add(workLine);
        waitWorkLine = setWorkLineListBaseInfo(waitWorkLine, projectInfo.getId(), type);

        //先找到第一个子节点的头
        List<FlowTree> children = flowTreeBase.getChildren() == null ? new ArrayList<>() : flowTreeBase.getChildren();
        if (null == children || children.isEmpty()) {
            log.error("WorkLineServiceImpl.initWorkLine(): 没找到头节点.");
            return false;
        }
        for (FlowTree flowTree : children) {
            //查询这个tree节点以及之后的所有和这个节点是同一科室的节点List
            //该方法内部 递归调用了别的方法
            List<WorkLine> tempList = getWaitWorkLine(flowTree, projectInfo);
            setWorkLineListBaseInfo(tempList, projectInfo.getId(), type);
            waitWorkLine.addAll(tempList);
        }
        saveAllWithNotify(waitWorkLine, projectInfo, true, true);
        return true;
    }

    @Override
    public boolean startWorkLine(FlowTree flowTreeBase, String projectId, Integer type) {
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        if (null == projectInfo) {
            log.error("projectId[{}]没有查到数据.", projectId);
            return false;
        }
        return startWorkLine(flowTreeBase, projectInfo, type);
    }

    /**
     * 找到 入参 后面和这个入参科室相同的flowTree, 转换为workLine对象保存来下.
     * 如果后续的节点的来源是多个, 那么就到这个节点这里截止.
     * [该方法内部调用了递归方法]
     *
     * @param firstFlowTree
     * @return
     */
    private List<WorkLine> getWaitWorkLine(FlowTree firstFlowTree, ProjectInfo projectInfo) {
        List<WorkLine> workLines = new ArrayList<>(16);
        List<FlowTree> flowTrees = new ArrayList<>(16);
        //以parentId为key的map
        Map<String, List<FlowTree>> map = flowTreeService.selectMapByPId(firstFlowTree.getId());
        if (MapUtils.isEmpty(map)) {
            log.error("查询以pid为key的map失败, 结果为null.操作人[{}]", UserSession.getUserSession().getName());
        }
        // 调用递归方法
        addWaitFlowTree(firstFlowTree, flowTrees, map, projectInfo);
        if (null == flowTrees || flowTrees.isEmpty()) {
            log.error("本次执行寻找连续的科室相同的节点失败, 结果为empty.firstFlowTreeName[{}]", firstFlowTree.getName());
            return null;
        }
        //将flowTree对象转换为workLine对象。
        int num = 0;
        for (FlowTree flowTree : flowTrees) {
            WorkLine workLine = CreateObjUtils.create(WorkLine.class);
            workLine.converFlowTreeToWorkLine(flowTree);
            workLine.setNum(++num);
            workLines.add(workLine);
        }
        return workLines;
    }

    @Override
    public boolean deleteByProjectId(String projectId) {
        List<WorkLine> workLineList = workLineRepository.findByProjectIdAndIsDelete(projectId, Constants.STATUE_NORMAL);
        workLineList.forEach(w -> w.setStatus(Constants.STATUE_DEL));
        workLineRepository.saveAll(workLineList);
        return true;
    }

    @Override
    public void toNext(String projectId, String workLineId) {
        String sync = redisSync.getSync(projectId, 180, TimeUnit.SECONDS);
        if (StringUtils.isBlank(sync)) {
            log.error("用户[{}]执行提交下一步获取锁失败.projectId[{}], workLineId[{}]",
                    UserSession.getUserSession().getName(), projectId, workLineId);
            return;
        }
        try {
            //将一些到处都要用的数据提前查出来使用.
            WorkLine workLine = workLineRepository.findByIdAndIsDelete(workLineId, Constants.STATUE_NORMAL)
                    .orElse(null);
            if (null == workLine) {
                log.error("WorkLineServiceImpl.toNext():error,通过workLineId[{}]没有查到对象.", workLineId);
                return;
            }
            List<WorkLine> byGroups = workLineRepository.findByProjectIdAndGroupsInAndIsDeleteOrderByNum(workLine.getProjectId(), Arrays.asList(workLine.getGroups()), Constants.STATUE_NORMAL);
            if (null == byGroups || byGroups.isEmpty()) {
                log.error("WorkLineServiceImpl.updateWorkLineStatus():error,通过byId.getProjectId()[{}], byId.getGroups()[{}]" +
                        "没有查到对象.", workLine.getProjectId(), workLine.getGroups());
                return;
            }
            ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
            if (null == projectInfo) {
                log.error("projectId[{}]没有查到数据.", projectId);
                return;
            }
            //1. 是否都处理了
            boolean allDid = isAllDid(workLine, "result");
            if (!allDid) {
                //1.1 没有
                log.error("toNext()error: projectId[{}] 的 workLineId[{}]没有都完成.", projectId, workLineId);
                throw new CustomException(CustomHttpStatus.NOT_ALL_OK.value(), CustomHttpStatus.NOT_ALL_OK.msg());
            }
            //全部确认了, 就将status 改为2
            //如果返回false, 就说明已经操作过了.或参数条件不够.
            boolean updateTemp = updateWorkLineStatus(byGroups, Constants.WORKLINE_STATUS_DID);
            if (!updateTemp) {
                log.error("toNext()error: 用户[{}],projectId[{}]在重复提交.", UserSession.getUserSession().getName(), projectId);
                return;
            }
            Integer type = workLine.getType();
            //1.2 判断下一个tree节点.
            List<FlowTree> flowTrees = flowTreeService.selectByPId(workLine.getFlowTreeId());
            if (null == flowTrees || flowTrees.isEmpty()) {
                // 对应1.2.1 子流程走完了.将子流程对应到的[主流程流水线节点]的状态和结果都更新为完成.
                updateMainWorkLineStatus(projectId, workLine.getFlowTreeId(), 2, 1);
                // 获得下一个主流程节点的头对象.可能是多个
                List<FlowTree> flowTreeList = flowTreeService.selectNextMainNode(workLine.getFlowTreeId());
                if (null == flowTreeList || flowTreeList.isEmpty()) {
                    //没有下一个主流程节点了, 说明流程完了.
                    log.info("projectId[{}]流程走完了.", projectId);
                    projectInfoService.updateResult(projectId, Constants.PROJECT_RESULT_OVER);
                    return;
                }
                // 判断主节点是否可以添加.
                for (FlowTree flowTree : flowTreeList) {
                    boolean did = isAllDidByFlowTree(projectId, flowTree, "status");
                    if (did) {
                        startWorkLine(flowTree, projectInfo, type);
                    }
                }
                //本if的范围内走完了, 退出.
                return;
            }
            // 子流程没走完, 拿到了所有的下一个tree节点
            for (FlowTree flowTree : flowTrees) {
                // 判断下一个tree节点是否是菱形的节点, 即要判断当前项目是区级/市级/省级
                Integer flowTreeGrade = flowTree.getGrade() == null ? 0 : flowTree.getGrade();
                //标识下一个节点是否是菱形节点
                boolean gradeNode = !Constants.PROJECT_GRADE_NONE.equals(flowTreeGrade);
                if (gradeNode &&
                        !projectInfo.getGrade().equals(flowTreeGrade)) {
                    //隶属关系不是0(为菱形节点), 也和项目中的记录不相等, 跳过.
                    continue;
                }
                // 隶属关系判断是匹配的, 或者不需要判断隶属关系. 即 0.
                // 往下走
                // 过滤隶属关系节点的功能在 getWaitWorkLine()->addWaitFlowTree() 中执行.
                // 对应1.2.2 判断这个节点是否可以创建了
                boolean allDidByFlowTree = isAllDidByFlowTree(projectId, flowTree, "status");
                //2.2.1 不可创建
                if (!allDidByFlowTree) {
                    continue;
                }
                //2.2.2 可以创建
                List<WorkLine> waitWorkLine = null;
                if (gradeNode) {
                    // 是菱形节点, 并且这个菱形节点通过了校验, 那么就要创建这个菱形节点的孩子节点.
                    List<FlowTree> gradeFlowTrees = flowTreeService.selectByPId(flowTree.getId());
                    for (FlowTree gradeFlowTree : gradeFlowTrees) {
                        // 该方法内部调用了递归方法
                        waitWorkLine = getWaitWorkLine(gradeFlowTree, projectInfo);
                        setWorkLineListBaseInfo(waitWorkLine, projectId, type);
                        saveAllWithNotify(waitWorkLine, projectInfo, false, true);
                    }
                } else {
                    // 不是菱形节点, 正常处理
                    // 该方法内部调用了递归方法
                    waitWorkLine = getWaitWorkLine(flowTree, projectInfo);
                    setWorkLineListBaseInfo(waitWorkLine, projectId, type);
                    saveAllWithNotify(waitWorkLine, projectInfo, false, true);
                }
            }
        } finally {
            redisSync.removeSync(projectId, sync);
        }
    }

    /**
     * 将检查通过的这些已生成的流水线节点的status 更改为 已完成。
     * 若结果已经是结束, 就说明是重复提交.返回.
     *
     * @param byGroups
     * @param status
     */
    private boolean updateWorkLineStatus(List<WorkLine> byGroups, int status) {
        for (WorkLine workLine : byGroups) {
            if (Constants.WORKLINE_STATUS_DID.equals(workLine.getStatus())) {
                //如果记录里已经有是2的, 说明已经操作过了.不能在操作.
                return false;
            }
            workLine.setStatus(status);
        }
        workLineRepository.saveAll(byGroups);
        return true;
    }

    /**
     * 通过tree节点id, 更新对应的主流程流水线节点的status和result
     *
     * @param flowTreeId
     * @param status
     */
    private void updateMainWorkLineStatus(String projectId, String flowTreeId, Integer status, Integer result) {
        FlowTree flowTree = flowTreeRepository.findById(flowTreeId).orElse(null);
        if (flowTree == null) {
            log.error("flowTreeId:{},查询不到flowTree对象.", flowTreeId);
            return;
        }
        String topId = flowTree.getTopId();
        List<WorkLine> byFlowTreeIdIn = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectId, CommonUtils.StringToList(topId, ","), Constants.STATUE_NORMAL);
        if (null == byFlowTreeIdIn || byFlowTreeIdIn.isEmpty()) {
            log.error("flowTreeId: {},对应的主流程节点查不到workLine对象.", flowTree.getId());
            return;
        }
        if (byFlowTreeIdIn.size() != 1) {
            log.error("flowTreeId: {},对应的主流程节点有多于1个workLine对象.", flowTree.getId());
        }
        WorkLine baseMain = byFlowTreeIdIn.get(0);
        baseMain.setStatus(status);
        baseMain.setResult(result);
        baseMain.setOperatorName(UserSession.getUserSession().getName());
        workLineRepository.save(baseMain);
        // 更新耗时表中主节点的耗时信息.
        spendTimeService.update(baseMain);
    }

    @Override
    public boolean completeWorkLine(WorkLine workLine) {
        WorkLine dateBase = workLineRepository.findByIdAndIsDelete(workLine.getId(), Constants.STATUE_NORMAL)
                .orElse(null);

        if (null == dateBase) {
            log.error("没查到对象.");
            return false;
        }
        if (!dateBase.getResult().equals(Constants.WORKLINE_STATUS_WAITDO)) {
            //现有状态不是 0未处理, 就说明处理过了, 不再处理.
            return false;
        }
        if (StringUtils.isBlank(workLine.getOperatorName())) {
            workLine.setOperatorName(UserSession.getUserSession().getName());
        }
        UpdateUtils.copyNonNullProperties(dateBase, workLine);
        // 设置更新时间.
        workLine.setUpdateTime(CommonUtils.getStringDate(new Date()));
        workLineRepository.save(workLine);
        // 判断这个子流程节点对应的主流程节点是否在spendTime
        spendTimeService.addMainWorkLine(workLine);
        return true;
    }

    @Override
    public List<WorkLine> getByProjectIdAndDeptId(Map<String, String> map) {
        String projectId = map.get("projectId");
        String groups = map.get("groups");
        List<WorkLine> lines = workLineDao.findByProjectIdAndGroupsOrderByNum(projectId, groups);
        if (lines == null) {
            return new ArrayList<>();
        }
        //填充备注.
        for (WorkLine workLine : lines) {
            List<WorkRecord> byRemark = workRecordService.findByWorkLineIdAndType(workLine.getId(), 0);
            workLine.setRemarkList(byRemark);
        }
        return lines;
    }

    @Override
    public PageVO<BacklogVO> getBaseInfoFromDeptId(Map<String, String> params) {
        String deptId = UserSession.getUserSession().getDeptId();
        if (StringUtils.isBlank(deptId)) {
            log.error("WorkLineServiceImpl.getBaseInfoFromDeptId(): error, 登录用户[{}]部门Id为null",
                    UserSession.getUserSession().getUserId());
            return null;
        }
        String flag = UserSession.getUserSession().getFlag();
        if (Constants.USER_FLAG_STAFF.equals(flag)) {
            params.put("deptId", deptId);
            return getByKeShi(params);
        } else if (Constants.USER_FLAG_DEPT.equals(flag)) {
            Department department = deptRepository.findById(deptId).orElse(new Department());
            deptId = "0".equals(department.getParentId()) ? department.getId() : department.getParentId();
            params.put("unitIds", deptId);
            return getByBuMen(params);
        }
        return null;
    }

    /**
     * 科室查询待办/已办
     *
     * @param params
     * @return
     */
    private PageVO<BacklogVO> getByKeShi(Map<String, String> params) {
        PageVO<BacklogVO> backlog = workLineDao.getBacklogForKeShi(params);
        backlog.getItems().forEach(backlogVO -> {
            FlowTree flowTree = flowTreeRepository.findById(backlogVO.getTreeTopId()).orElse(null);
            if (flowTree != null) {
                String workLineName = flowTree.getName();
                backlogVO.setWorkLineName(workLineName);
            }
        });
        return backlog;
    }

    /**
     * 部门查询待办/已办
     *
     * @param params
     * @return
     */
    private PageVO<BacklogVO> getByBuMen(Map<String, String> params) {
        PageVO<BacklogVO> backlog = workLineDao.getBacklogForBuMen(params);
        return backlog;
    }

    @Override
    public boolean isAllDid(WorkLine workLine, String condition) {
        return isDid(Arrays.asList(workLine), condition);
    }

    @Override
    public boolean isAllDidByFlowTree(String projectId, FlowTree flowTree, String condition) {
        List<String> parentIds = CommonUtils.StringToList(flowTree.getParentId(), ",");
        List<WorkLine> workLines = workLineRepository.findByProjectIdAndFlowTreeIdInAndIsDelete(projectId, parentIds, Constants.STATUE_NORMAL);
        if (CollectionUtils.isEmpty(workLines)) {
            log.error("projectId[{}] + flowTreeId[{}] 查询结果为null", projectId, flowTree.getParentId());
            return false;
        }
        if (null != workLines && null != parentIds && parentIds.size() != workLines.size()) {
            //记录中的数量 和 parentIds 的数量不相等, 可能是因为有点前置节点还没创建.
            log.info("parentIds is [{}], but had workLines is [{}]", parentIds.size(), workLines.size());
            return false;
        }
        return isDid(workLines, condition);
    }

    /**
     * 有未完成的返回 false, 都完成了返回true
     * 查询这个workLine的兄弟们是否都完成了.
     *
     * @param workLine
     * @return
     */
    private boolean isDid(List<WorkLine> workLines, String condition) {
        if (CollectionUtils.isEmpty(workLines)) {
            log.error("WorkLineServiceImpl.isDid error, List<WorkLine> is null");
            return false;
        }
        List<String> groupsList = workLines.stream().map(WorkLine::getGroups).collect(Collectors.toList());
        String projectId = workLines.get(0).getProjectId();
        List<WorkLine> byGroups = workLineRepository.findByProjectIdAndGroupsInAndIsDeleteOrderByNum(projectId, groupsList, Constants.STATUE_NORMAL);
        // 通过status来判断, 是判断前置节点是否都完成了.
        if ("status".equals(condition)) {
            for (WorkLine temp : byGroups) {
                if (temp.getStatus() != 2) {
                    return false;
                }
            }
        }
        //通过result来判断, 是判断同一个groups下的兄弟节点是否都完成了.
        if ("result".equals(condition)) {
            for (WorkLine temp : byGroups) {
                if (temp.getResult() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PreviewVO getPlan(String projectId, Integer level, String flowTreeId) {
        return planService.getByLevel(projectId, level, flowTreeId);
    }

    /**
     * 递归调用的方法
     * 判断当前节点是否只有一个孩子, 若不是则返回. 若是, 再判断是否是同一个科室, 若是,再判断下一个节点
     * 是否只有一个来源.
     *
     * @param currentFlowTree 每次递归判断的
     * @param flowTrees       最终获取到的, 要转换为workLine的集合
     * @param map             以parentId为Key 的映射, 可以获得 某节点id的孩子.
     * @param projectInfo     项目信息
     */
    private void addWaitFlowTree(FlowTree currentFlowTree, List<FlowTree> flowTrees, Map<String, List<FlowTree>> map,
                                 ProjectInfo projectInfo) {
        FlowTree flowTree = new FlowTree();
        UpdateUtils.copyNonNullProperties(currentFlowTree, flowTree);
        if (null == flowTree.getGrade() || null == flowTree.getType()) {
            log.error("flowTree.getGrade() || flowTree.getType() is null, flowTreeId[{}]", flowTree.getId());
            return;
        }
        flowTree = setFlowTreeToRealDeptId(flowTree, projectInfo);
        //不是隶属关系的节点, 才添加
        if (Constants.PROJECT_GRADE_NONE.equals(flowTree.getGrade())) {
            flowTrees.add(flowTree);
        }
        if (StringUtils.isBlank(flowTree.getDeptIds())) {
            log.error("firstFlowTree[{}]映射到的deptId为null", flowTree.getId());
            return;
        }
        //从刘接口查询到map中查询以pid为key的tree节点.
        List<FlowTree> flowTreeList = map.get(flowTree.getId());
        //size == 0
        if (null == flowTreeList || flowTreeList.isEmpty()) {
            return;
        }
        //这里flowTreeList.size() > 1说明在这个节点开始发散了, 他有多个孩子.
        if (flowTreeList.size() > 1) {
            return;
        }
        //当前节点只有一个孩子节点.可以继续判断.
        FlowTree next = flowTreeList.get(0);
        //先判断一下下一个节点是否是街办处理,若是则可以过.
        //如果是科室处理, 科室一样也可以过.
        //如果是菱形节点, 那么就到此为止, 让这种判断交给判断下一步去执行.
        if (!Constants.PROJECT_GRADE_NONE.equals(next.getGrade())) {
            //是菱形节点, 到此为止
            return;
        }
        //如果下一个节点是街办处理或科室处理, 将真实单位设置进去进行后续判断.
        next = setFlowTreeToRealDeptId(next, projectInfo);
        if (flowTree.getDeptIds().equals(next.getDeptIds())) {
            if (next.getParentId().contains(",")) {
                //如果下一个的来源是多个, 那么就到此为止, 即合并节点
                return;
            } else {
                if (next.getParentId().length() >= 25) {
                    log.error("next.parentId[{}]判断没有,分割.", next.getParentId());
                }
                //下一个的来源是一个,就是下个节点的父亲是当前节点, 并且当前节点只有一个孩子
                //递归调用
                addWaitFlowTree(next, flowTrees, map, projectInfo);
                return;
            }
        } else {
            //不符合前置条件.
            return;
        }
    }

    /**
     * 给这个list中的workLine对象设置关联的这2个Id, groups, status, result
     *
     * @param waitWorkLine
     * @param projectId
     * @param type
     */
    private List<WorkLine> setWorkLineListBaseInfo(List<WorkLine> waitWorkLine, String projectId, Integer type) {
        String groups = IdWorker.createId();
        waitWorkLine.forEach(w -> {
            w.setProjectId(projectId);
            w.setGroups(groups);
            w.setType(type);
            w.setStatus(1);
            w.setResult(0);
        });
        return waitWorkLine;
    }

    /**
     * 对这个节点进行类型判断. 是街办处理 还是 责任科室处理.
     * @param source
     * @param projectInfo
     * @return
     */
    private FlowTree setFlowTreeToRealDeptId(FlowTree source, ProjectInfo projectInfo) {
        //如果该节点是街办处理, 那么要取真实的处理单位去做后续比较.
        if (Constants.TODO_JIEBAN.equals(source.getType())) {
            //如果取的是真实的, 那么该节点的unitId需要独立查询一次.
            //再将处理单位设置到flowTree中去
            String projectDeptIds = projectInfo.getStreetOffice();
            CheckUtil.isBlank(projectDeptIds, 500, Constants.JIEBAN_IS_NULL);
            Department department = deptRepository.findById(projectDeptIds).orElse(new Department());
            source.setUnitIds(department.getParentId());
            source.setDeptIds(projectDeptIds);
        } else if(Constants.TODO_ZERENKESHI.equals(source.getType())){
            // 如果该节点是责任单位的科室处理, 那么取真实的数据填充进去.
            String projectDeptIds = projectInfo.getResponsibleDept();
            CheckUtil.isBlank(projectDeptIds, 500, Constants.ZERENKESHI_IS_NULL);
            Department department = deptRepository.findById(projectDeptIds).orElse(new Department());
            source.setUnitIds(department.getParentId());
            source.setDeptIds(projectDeptIds);
        }
        return source;
    }

    @Override
    public boolean resetChildWorkLine(String projectId, String treeTopId) {
        if ("0".equals(treeTopId)) {
            // 正常不应该触发, 前端也获取不到.
            log.error("用户[{}]在操作项目[{}]子流程退回, 但传入的treeTopId为0", UserSession.getUserSession().getName(), projectId);
            return false;
        }
        WorkLine mainWorkLine = workLineRepository.findByProjectIdAndFlowTreeIdAndIsDelete(projectId, treeTopId, Constants.STATUE_NORMAL);
        if (Constants.WORKLINE_STATUS_DID == mainWorkLine.getStatus()) {
            //该环节已完成.不可再撤销了
            log.error("用户[{}]在操作项目[{}]子流程退回, 但该环节[{}]已结束.", UserSession.getUserSession().getName(), projectId);
            return false;
        }
        // 该环节可以执行退回.主节点也要逻辑删除.
        mainWorkLine.setIsDelete(Constants.STATUE_DEL);
        List<WorkLine> waitDeleteList = workLineRepository.findByProjectIdAndTreeTopIdAndIsDelete(projectId, treeTopId, Constants.STATUE_NORMAL);
        List<String> workLineIdList = new ArrayList<>(waitDeleteList.size());
        for (WorkLine workLine : waitDeleteList) {
            workLine.setIsDelete(Constants.STATUE_DEL);
            workLineIdList.add(workLine.getId());
        }
        // 将对主节点的操作也保存进去
        waitDeleteList.add(mainWorkLine);
        workLineRepository.saveAll(waitDeleteList);
        // 删除workRecord表中的记录.
        workRecordService.deleteWorkRecordFromWorkLineList(workLineIdList);
        // 重新开启子流程
        ProjectInfo projectInfo = projectInfoService.getById(projectId);
        FlowTree flowTree = flowTreeService.selectFlowTreeAndChildren(treeTopId);
        return startWorkLine(flowTree, projectInfo, projectInfo.getType());
    }

    /**
     * 发送消息通知
     * 后面的 boolean 标识是否给对应的发送.
     *
     * @param workLineList
     * @param projectInfo
     */
    @Override
    public void saveAllWithNotify(List<WorkLine> workLineList, ProjectInfo projectInfo, boolean project, boolean backlog) {
        if (CollectionUtils.isEmpty(workLineList)) {
            return;
        }
        // 将待办的节点落库
        workLineDao.saveAll(500, workLineList);
        // 构造推送消息的数据.
        // 获取一级环节的名称.
        WorkLine workLineTemp = workLineList.get(0);
        String treeTopId = workLineTemp.getTreeTopId();
        // 第一个节点如果是主节点的话其treeTopId是0
        if("0".equals(treeTopId) && workLineList.size() > 1){
            treeTopId = workLineList.get(1).getTreeTopId();
        }
        String mainWorkLineName = flowTreeRepository.findById(treeTopId).orElse(new FlowTree()).getName();
        mainWorkLineName = StringUtils.isBlank(mainWorkLineName) ? "" : mainWorkLineName;
        if(null == projectInfo.getStatus()){
            projectInfo = projectInfoRepository.findById(projectInfo.getId()).orElse(new ProjectInfo());
        }
        if(project){
            // 构造发送给项目单位的通知
            ProjectEvent projectEvent = new ProjectEvent();
            projectEvent.setProjectId(projectInfo.getId());
            projectEvent.setProjectName(projectInfo.getName());
            projectEvent.setProjectStatus(projectInfo.getStatus());
            projectEvent.setProjectGroupId(projectInfo.getGroupId());
            projectEvent.setProjectDeptId(projectInfo.getResponsibleDept());
            projectEvent.setProjectIsGroup(projectInfo.getIsGroup());
            projectEvent.setMainWorkLineName(mainWorkLineName);
            eventBus.post(projectEvent);
        }

        if (backlog){
            //构造发送给待办科室的通知.
            Set<String> repeatGroups = new HashSet<>(workLineList.size());
            for (WorkLine workLine : workLineList) {
                // 添加失败, 去重
                if(!repeatGroups.add(workLine.getGroups())){
                    continue;
                }
                BacklogEvent backlogEvent = new BacklogEvent();
                backlogEvent.setProjectId(projectInfo.getId());
                backlogEvent.setProjectName(projectInfo.getName());
                backlogEvent.setStatus(projectInfo.getStatus());
                backlogEvent.setBacklogDeptId(workLine.getDeptIds());
                backlogEvent.setProjectIsGroup(projectInfo.getIsGroup());
                backlogEvent.setGroups(workLine.getGroups());
                backlogEvent.setWorkLineName(workLine.getName());
                backlogEvent.setMainWorkLineName(mainWorkLineName);
                eventBus.post(backlogEvent);
            }
        }
    }
}
