package com.taoding.mp.core.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.dao.UserRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.entity.User;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.FlowWorkFileRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.flow.vo.ViewDataVO;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.dao.WorkRecordRepository;
import com.taoding.mp.core.work.entity.SpendTime;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.service.PlanService;
import com.taoding.mp.core.work.service.SpendTimeService;
import com.taoding.mp.core.work.vo.AffixVO;
import com.taoding.mp.core.work.vo.PlanVO;
import com.taoding.mp.core.work.vo.WorkLineVO;
import com.taoding.mp.util.CheckUtil;
import com.taoding.mp.util.UpdateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: youngsapling
 * @date: 2019-04-25
 * @modifyTime:
 * @description:
 */
@Service
@Slf4j
public class PlanServiceImpl implements PlanService {
    @Autowired
    ProjectInfoRepository projectInfoRepository;
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
    FlowTreeService flowTreeService;
    @Autowired
    SpendTimeService spendTimeService;

    @Override
    public PreviewVO getByLevel(String projectId, Integer level, String flowTreeId) {
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        if (null == projectInfo) {
            log.error("查询办理进度时项目projectId[{}]不存在.", projectInfo);
            return new PreviewVO();
        }
        String type = "";
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)) {
            //是主流程
            type = projectInfo.getType().toString();
        } else if (Constants.FLOW_NODE_SUB_LEVEL.equals(level)) {
            //是子流程
            type = flowTreeId;
        }
        if (StringUtils.isBlank(type)) {
            log.error("传入的level[{}]不对.", level);
        }

        // 拿到视图
        PreviewVO from = flowTreeService.viewByApp(type, level);
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
            workLineList = converForMain(flowTrees, didWorkLineMap, workLineList, projectId);
        } else if (Constants.FLOW_NODE_SUB_LEVEL.equals(level)) {
            //是子流程
            workLineList = converForChild(flowTrees, didWorkLineMap, workLineList, projectInfo);
        }
        from.setWorkLineList(workLineList);
        return from;
    }

    @Override
    public PlanVO get(String flowTreeId, String workLineId) {
        FlowTree oldFlowTree = flowTreeRepository.findById(flowTreeId).orElse(null);
        if (null == oldFlowTree) {
            log.error("flowTreeId[{}]没有查询到对象.", flowTreeId);
            return null;
        }
        FlowTree flowTree = new FlowTree();
        UpdateUtils.copyNonNullProperties(oldFlowTree, flowTree);
        WorkLine workLine;
        workLine = StringUtils.isNotBlank(workLineId) ? workLineRepository.findByIdAndIsDelete(workLineId,
                Constants.STATUE_NORMAL).orElse(null) : null;
        PlanVO planVO = new PlanVO();
        planVO.setFlowTreeRemark(flowTree.getRemark() == null ? "" : flowTree.getRemark());
        if (null != workLine) {
            Map<Integer, List<WorkRecord>> collect = workRecordRepository.findByWorkLineIdAndIsDelete(workLineId, Constants.STATUE_NORMAL)
                    .stream().collect(Collectors.groupingBy(WorkRecord::getType));
            List<WorkRecord> remarkList = null == collect.get(Integer.valueOf("0")) ? new ArrayList<>() : collect.get(Integer.valueOf("0"));
            if (StringUtils.isNotBlank(workLine.getRemark())) {
                //用户点击完成或跳过后填写的备注是在workLine上挂着, 转换过来.
                WorkRecord workRecord = new WorkRecord();
                remarkList.add(workRecord);
                workRecord.setCreateTime(workLine.getUpdateTime());
                workRecord.setRemark(workLine.getRemark());
                workRecord.setOperatorName(workLine.getOperatorName());
            }
            planVO.setRemarkList(remarkList);

            //设置申报材料
            List<WorkRecord> dataList = collect.get(Integer.valueOf("1"));
            if (CollectionUtils.isNotEmpty(dataList)) {
                dataList.forEach(w -> {
                    if (StringUtils.isNotBlank(w.getAffix())) {
                        w.setAffixList(JSONObject.parseArray(w.getAffix(), AffixVO.class));
                    }
                });
            } else {
                dataList = new ArrayList<>();
            }
            // 查询模板中的全部需要的申报材料
            List<FlowWorkFile> flowWorkFileList = flowWorkFileRepository.findAllByFlowTreeIdAndIsDelete(flowTreeId, Constants.STATUE_NORMAL);
            List<WorkRecord> allDataList = new ArrayList<>(flowWorkFileList.size());
            for (FlowWorkFile flowWorkFile : flowWorkFileList) {
                boolean contain = false;
                for (WorkRecord workRecord : dataList) {
                    contain = flowWorkFile.getId().equals(workRecord.getFlowWorkFileId());
                    if (contain) {
                        workRecord.setAffix(null);
                        allDataList.add(workRecord);
                        break;
                    }
                }
                if (!contain) {
                    WorkRecord workRecord = new WorkRecord();
                    workRecord.setFlowWorkFileName(flowWorkFile.getName());
                    workRecord.setFlowWorkFileId(flowWorkFile.getId());
                    workRecord.setAffixList(new ArrayList<>());
                    allDataList.add(workRecord);
                }
            }
            planVO.setDataList(allDataList);
        } else {
            workLine = new WorkLine();
            planVO.setDataList(new ArrayList<>());
            planVO.setRemarkList(new ArrayList<>());
        }
        String deptIds = StringUtils.isBlank(workLine.getDeptIds()) ? flowTree.getDeptIds() : workLine.getDeptIds();
        setPeople(planVO, deptIds);
        return planVO;
    }

    /**
     * 设置该节点对应的
     *
     * @param planVO
     */
    private void setPeople(PlanVO planVO, String deptIds) {
        List<User> allUser = new ArrayList<>();
        User dept = new User();
        User staff = new User();
        if (StringUtils.isNotBlank(deptIds)) {
            Department staffDept = deptRepository.findById(deptIds).orElse(new Department());
            allUser = userRepository.findByDeptId(staffDept.getId()) == null ? new ArrayList<>() : userRepository.findByDeptId(staffDept.getId());
            String parentId = staffDept.getParentId() == null ? "" : staffDept.getParentId();
            if (!Constants.DEPARTMENT_PARENTID_NOTEXIST.equals(parentId)) {
                Department deptId = deptRepository.findById(parentId).orElse(new Department());
                allUser.addAll(userRepository.findByDeptId(deptId.getId()));
            }
            for (User user : allUser) {
                if (Constants.USER_FLAG_STAFF.equals(user.getFlag())) {
                    staff = user;
                } else if (Constants.USER_FLAG_DEPT.equals(user.getFlag())) {
                    dept = user;
                }
            }
        }
        planVO.setDept(dept);
        planVO.setStaff(staff);
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
                               List<Map<String, Object>> workLineList, String projectId) {
        List<Department> taoding = deptRepository.findAllByCorpIdOrderByNumDesc("taoding");
        Map<String, String> deptNameMap = new HashMap<>(taoding.size());
        taoding.stream().forEach(department -> deptNameMap.put(department.getId(), department.getName()));
        List<WorkLineVO> tempWorkLine = new ArrayList<>(flowTrees.size());

        //构造
        for (Map.Entry<String, FlowTree> entry : flowTrees.entrySet()) {
            FlowTree flowTree = entry.getValue();
            WorkLine workLine = didWorkLineMap.get(flowTree.getId());
            if (null == workLine) {
                //还没有flowTree对应的workLine
                workLine = new WorkLine();
                workLine.converFlowTreeToWorkLine(flowTree);
                workLine.setStatus(Constants.WORKLINE_STATUS_WAITDO);
            } else {
                //已经有flowTree对应的workLine
                workLine.converFlowTreeToWorkLine(flowTree);
            }
            WorkLineVO vo = new WorkLineVO();
            UpdateUtils.copyNonNullProperties(workLine, vo);
            //因为是主流程, 所以查询的是单位id
            vo.setDeptName(deptNameMap.get(vo.getUnitIds()));
            tempWorkLine.add(vo);
        }

        // 将逾期情况注入进去.
        tempWorkLine = parseOverDue(tempWorkLine, projectId);

        // 转换为返回对象.
        for (WorkLineVO vo : tempWorkLine) {
            Map<String, Object> temp = new HashMap<>(2);
            temp.put("key", vo.getFlowTreeId());
            temp.put("object", vo);
            workLineList.add(temp);
        }
        return workLineList;
    }

    private List<WorkLineVO> parseOverDue(List<WorkLineVO> tempWorkLine, String projectId) {
        List<SpendTime> spendTimes = spendTimeService.findByProjectIdAndIsDelete(projectId, Constants.STATUE_NORMAL);
        // 构造map直接调用.
        Map<String, SpendTime> spendTimeMap = new HashMap<>(spendTimes.size());
        spendTimes.forEach(spendTime -> spendTimeMap.put(spendTime.getWorkLineId(), spendTime));

        SpendTime spendTime;
        for (WorkLineVO vo : tempWorkLine) {
            if (Constants.WORKLINE_STATUS_DID.equals(vo.getStatus())) {
                spendTime = spendTimeMap.get(vo.getId()) == null ? new SpendTime() : spendTimeMap.get(vo.getId());
                // 已办的情况
                vo.setOverdue(spendTime.getOverdue());
                vo.setSpend(spendTime.getSpend());
            }
            if (Constants.WORKLINE_STATUS_DOING.equals(vo.getStatus())) {
                // 处理中的, 重新计算然后展示
                spendTime = spendTimeMap.get(vo.getId());
                if (null == spendTime) {
                    // 耗时表中没有这个记录, 说明这个处理中的一级节点还没有开启计时.
                    vo.setOverdue(Constants.NOT_OVERDUE);
                    vo.setSpend(0);
                } else {
                    WorkLine workLine = new WorkLine();
                    BeanUtils.copyProperties(vo, workLine);
                    SpendTime calculate = spendTimeService.calculate(workLine);
                    vo.setOverdue(calculate.getOverdue());
                    vo.setSpend(calculate.getSpend());
                }
            }
        }
        return tempWorkLine;
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
        Map<String, Department> deptNameMap = new HashMap<>(taoding.size());
        taoding.stream().forEach(department -> deptNameMap.put(department.getId(), department));

        for (Map.Entry<String, FlowTree> entry : flowTrees.entrySet()) {
            FlowTree flowTree = entry.getValue();
            WorkLine workLine = didWorkLineMap.get(flowTree.getId());
            WorkLineVO vo = new WorkLineVO();
            vo.setGrade(null == flowTree.getGrade() ? 0 : flowTree.getGrade());

            if (null == workLine) {
                //还没有flowTree对应的workLine
                vo.converFlowTreeToWorkLine(flowTree);
                vo.setStatus(0);
                vo.setHasRemarkList(0);
                vo.setHasDataList(0);
                vo.setHasFlowTreeRemark(0);
                vo.setExistWorkLine(0);
                if (Constants.TODO_JIEBAN.equals(flowTree.getType())) {
                    //如果是街办, 那么去主流程中查询.
                    String deptId = projectInfo.getStreetOffice();
                    CheckUtil.isBlank(deptId, 500, Constants.JIEBAN_IS_NULL);
                    String unitId = deptNameMap.getOrDefault(deptId, new Department()).getParentId();
                    vo.setDeptIds(deptId);
                    vo.setUnitIds(unitId);
                } else if (Constants.TODO_ZERENKESHI.equals(flowTree.getType())) {
                    //如果是责任科室, 那么去主流程中查询.
                    String deptId = projectInfo.getResponsibleDept();
                    CheckUtil.isBlank(deptId, 500, Constants.ZERENKESHI_IS_NULL);
                    String unitId = deptNameMap.getOrDefault(deptId, new Department()).getParentId();
                    vo.setDeptIds(deptId);
                    vo.setUnitIds(unitId);
                }
            } else {
                //已经有flowTree对应的workLine
                vo.converFlowTreeToWorkLine(flowTree);
                UpdateUtils.copyNonNullProperties(workLine, vo);
                //判断有无 备注 或 申报材料
                setRemarkAndData(workLine.getId(), vo);
                //判断有无节点备注
                vo.setHasFlowTreeRemark(StringUtils.isBlank(flowTree.getRemark()) ? 0 : 1);
                vo.setExistWorkLine(1);
            }

            //设置办理单位名称.
            String unitName = deptNameMap.getOrDefault(vo.getUnitIds(), new Department()).getName();
            unitName = unitName == null ? "" : unitName;
            String depteName = deptNameMap.getOrDefault(vo.getDeptIds(), new Department()).getName();
            depteName = depteName == null ? "" : ">>" + depteName;
            String showName = unitName + depteName;
            if (StringUtils.isBlank(showName)) {
                log.error("科室办理的depteName is null, backlogDeptId[{}]", vo.getDeptIds());
            }
            vo.setDeptName(showName);

            // 在这里人为修改了返回的结果标识.
            if (Constants.FAILURE.equals(vo.getExistWorkLine())) {
                // 4标识为未创建workLine节点.
                vo.setStatus(Constants.NOT_EXIST_WORKLINE);
            } else {
                vo.setStatus(vo.getResult() == null ? 0 : vo.getResult());
            }
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
}
