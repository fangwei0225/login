package com.taoding.mp.core.work.service.impl;

import com.google.common.base.Strings;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.VersionIteratorRepository;
import com.taoding.mp.core.flow.dao.VersionUpdateFileRelRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.VersionIteratorRel;
import com.taoding.mp.core.flow.entity.VersionUpdateFileRel;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.project.dao.ProjectInfoRepository;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.work.dao.*;
import com.taoding.mp.core.work.entity.SpendTime;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.service.EvolutionService;
import com.taoding.mp.core.work.service.WorkLineService;
import com.taoding.mp.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: youngsapling
 * @date: 2019-04-20
 * @modifyTime:
 * @description: 用于版本切换
 */
@Service
@Slf4j
@Transactional(rollbackOn = {Exception.class})
public class EvolutionServiceImpl implements EvolutionService {
    @Autowired
    VersionIteratorRepository flowTreeMapRepository;
    @Autowired
    VersionUpdateFileRelRepository fileRelRepository;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    FlowTreeService flowTreeService;
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    WorkLineDAO workLineDAO;
    @Autowired
    WorkLineService workLineService;
    @Autowired
    WorkLineHistoryDAO workLineHistoryDao;
    @Autowired
    WorkRecordDAO workRecordDAO;
    @Autowired
    WorkRecordRepository workRecordRepository;
    @Autowired
    WorkRecordHistoryDAO workRecordHistoryDao;
    @Autowired
    RedisSync redisSync;
    @Autowired
    ProjectInfoRepository projectInfoRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    SpendTimeDAO spendTimeDAO;
    @Autowired
    SpendTimeHistoryDAO spendTimeHistoryDAO;

    @Override
    public boolean evolution(String newFlowModelId) {
        log.info("开始版本切换, 操作人[{}]=================", UserSession.getUserSession().getName());
        String sync = redisSync.getSync(newFlowModelId, 300, TimeUnit.SECONDS);
        if (StringUtils.isBlank(sync)) {
            log.error("版本更新时newFlowModelId[{}]在重复执行.", newFlowModelId);
            return false;
        }
        try {
            //通过新的modelId查询到本次更新的所有tree新旧主键映射关系
            List<VersionIteratorRel> versionIteratorRels = flowTreeMapRepository.findAllByNewFlowModeIdAndIsEffectAndIsDelete(newFlowModelId, 1, 1);
            if (CollectionUtils.isEmpty(versionIteratorRels)) {
                log.error("版本更新失败, 通过flowModelId[{}]没有找到flowTreeId的新旧映射关系.", newFlowModelId);
                return false;
            }
            //1.获得旧的flowModelId, 用途是从现在的表中直接找到[本次]要更新的对象.
            String oldFlowModelId = versionIteratorRels.get(0).getOldFlowModeId();
            if (StringUtils.isBlank(oldFlowModelId)) {
                log.error("oldFlowModelId is null. so error.=================");
                return false;
            }
            //2. flowTreeMap 存的是id 和 flowTree对象的映射
            List<FlowTree> flowTrees = flowTreeRepository.findByFlowModeIdAndIsDelete(newFlowModelId, 1);
            if (CollectionUtils.isEmpty(flowTrees)) {
                log.error("通过newFlowModelId找到的List<FlowTree> is null==================");
                return false;
            }
            Map<String, FlowTree> flowTreeMap = getFlowTreeMap(flowTrees);
            //3. treeIdMap 存的是新旧id的映射关系.
            Map<String, String> treeIdMap = getTreeIdMap(versionIteratorRels);
            // 123构造好了, 更新workLine.
            // 返回的是 4
            // [第一步]更新流水线节点
            Map<String, String> workLineMap = updateWorkLine(oldFlowModelId, newFlowModelId, flowTreeMap, treeIdMap);
            if (MapUtils.isEmpty(workLineMap)) {
                log.error("workLine节点在本次版本切换中没有发生变化,具体原因看log, 时间:[{}]", oldFlowModelId, CommonUtils.getStringDate(new Date()));
                return true;
            }
            // 5.flowWorkFileMap
            // 提前判断需要的条件是否满足
            List<VersionUpdateFileRel> fileRels = fileRelRepository.findByNewFlowModeIdAndIsEffectAndIsDelete(newFlowModelId, 1, 1);
            if (CollectionUtils.isEmpty(fileRels)) {
                log.error("通过newFlowModelId找到的List<VersionUpdateFileRel> is null.");
                return false;
            }
            Map<String, String> flowWorkFileMap = getFlowWorkFileMap(fileRels);
            // [第二步]更新申报材料
            updateWorkRecord(oldFlowModelId, newFlowModelId, workLineMap, treeIdMap, flowWorkFileMap);
            // [第三步]更新时间记录表
            updateSpendTime(oldFlowModelId, newFlowModelId, workLineMap);
            log.info("版本切换结束.===========================");
            return true;
        } finally {
            redisSync.removeSync(newFlowModelId, sync);
        }
    }

    /**
     * 更新workLine
     *
     * @param oldFlowModelId
     * @param flowTreeMap
     * @param treeIdMap
     * @return
     */
    private Map<String, String> updateWorkLine(String oldFlowModelId, String newFlowModelId,
                                               Map<String, FlowTree> flowTreeMap, Map<String, String> treeIdMap) {
        //通过oldFlowModelId找到本次要更新的数据.
        List<WorkLine> oldAll = workLineDAO.findByFlowModelId(oldFlowModelId);
        if (CollectionUtils.isEmpty(oldAll)) {
            log.error("updateWorkLine(): 通过oldFlowModelId[{}]没有查询到数据.============", oldFlowModelId);
            return new HashMap<>();
        }
        //保存到历史表中.
        workLineHistoryDao.saveAll(500, oldAll);
        List<WorkLine> all = new ArrayList<>(oldAll.size());

        //在数据库中删除获取到的list.
        workLineDAO.deleteByIdIn(oldAll.stream().map(WorkLine::getId).collect(Collectors.joining(",")));
        //构造4
        Map<String, String> workLineMap = new HashMap<>(all.size());
        //分组移除后还要将这个分组对应的顶级节点也移除, 但是这次移除无法在一次遍历中完成, 所以先记录, 完了后再遍历.
        //若有顶级节点会进入这个set中, 说明是有待办的子流程被删除了, 导致这个环节要重新走.
        Set<String> needDelete = new HashSet<>();
        //遍历all
        for (WorkLine workLine : oldAll) {
            WorkLine temp = new WorkLine();
            UpdateUtils.copyNonNullProperties(workLine, temp);
            String newId = treeIdMap.get(temp.getFlowTreeId());
            if (StringUtils.isBlank(newId)) {
                //说明在新版本中没有了, 判断是否处理中.
                //判断3.2.1 和 3.2.2的逻辑.
                updateDoing(temp, treeIdMap, needDelete);
            } else {
                //说明在新版本中有.拿新版本中的对象.
                FlowTree flowTree = flowTreeMap.get(newId);
                if (null != flowTree) {
                    //维护4
                    String workLineOldId = workLine.getId();
                    String workLineNewId = IdWorker.createId();
                    workLineMap.put(workLineOldId, workLineNewId);
                    //需要改的字段: flowTreeId, flowModelId, name, hasFlowWorkFile, treeTopId
                    temp.setId(workLineNewId);
                    temp.setFlowTreeId(flowTree.getId());
                    temp.setFlowModelId(flowTree.getFlowModeId());
                    temp.setName(flowTree.getName());
                    temp.setHasFlowWorkFile(flowTree.getHasFlowWorkFile());
                    temp.setTreeTopId(flowTree.getTopId());
                    all.add(temp);
                } else {
                    log.error("updateWorkLine:error, 新旧Id映射treeIdMap.get({})查到的newId在flowTreeMap.get({})中没有查询到新节点.", workLine.getFlowTreeId(), newId);
//                    throw new CustomException(CustomHttpStatus.INTERNAL_SERVER_ERROR.value(), "error");
                }
            }
        }

        //将需要移除的节点移除.(因为添加的时候放的是顶级节点的treeTopId, 所以删除的时候也按这个删除.)
        List<WorkLine> collect = all.stream().filter(e -> !needDelete.contains(e.getTreeTopId())).collect(Collectors.toList());
        //all的数据构造完成, 保存到数据库中.
        workLineDAO.saveAll(500, collect);
        return workLineMap;
    }

    /**
     * 如果某节点在新模板中没有, 则判断其是否是处理中. 如果是处理中, 则标识这个子流程中已有的数据要删除
     * 并且重新启动这个子流程.
     *
     * @param treeTopIdMap
     * @param workLine
     * @param treeIdMap
     * @param needDelete
     */
    private void updateDoing(WorkLine workLine, Map<String, String> treeIdMap, Set<String> needDelete) {
        if (workLine.getStatus() == 1) {
            String treeTopId = workLine.getTreeTopId();
            //还是处理中的
            if (needDelete.add(treeTopId)) {
                //其顶级节点添加成功
                //之前旧节点的父节点即1级节点, 找到这个1级节点对应的新节点, 然后按新节点重新走.
                String treeTopIdNewId = treeIdMap.get(treeTopId);
                if (StringUtils.isBlank(treeTopIdNewId)) {
                    // 对应的父节点在新模板中没有, 可能是删除了, 就跳过吧
                    log.error("updateDoing(): treeTopId[{}]没有查询到数据.", treeTopId);
                    log.error("对应的父节点在新模板中没有, 可能是没查到, 也有可能是新模板中就没有这个节点了.跳过.");
                    return;
                }
                // 查询到对应的新的顶级节点
                // 查询这个顶级节点以及他的孩子, 然后直接执行.
                FlowTree flowTree = flowTreeService.selectFlowTreeAndChildren(treeTopIdNewId);
                if (null == flowTree) {
                    log.error("updateDoing(): treeTopIdNewId[{}]查询到的节点为null.", treeTopIdNewId);
                    return;
                }
                //执行新的顶级节点的添加逻辑.
                workLineService.startWorkLine(flowTree, workLine.getProjectId(), workLine.getType());
            } else {
                //顶级节点已经在要删除的里了, 直接移除
            }
        } else {
            //不是处理中了, 直接移除
        }
    }

    /**
     * 更新workRecord
     *
     * @param oldFlowModelId
     * @param newFlowModelId
     * @param workLineMap
     * @param treeIdMap
     * @param flowWorkFileMap
     */
    private void updateWorkRecord(String oldFlowModelId, String newFlowModelId, Map<String, String> workLineMap,
                                  Map<String, String> treeIdMap, Map<String, String> flowWorkFileMap) {
        List<WorkRecord> oldAll = workRecordDAO.findByFlowModelId(oldFlowModelId);
        if (CollectionUtils.isEmpty(oldAll)) {
            log.error("updateWorkRecord(): 通过oldFlowModelId[{}]没有查询到数据.", oldFlowModelId);
            return;
        }
        //保存到历史记录表中
        workRecordHistoryDao.saveAll(500, oldAll);
        List<WorkRecord> all = new ArrayList<>(oldAll.size());
        //删除查出来的这些
        workRecordDAO.deleteIdIn(oldAll.stream().map(WorkRecord::getId).collect(Collectors.joining(",")));
        //遍历构造数据.
        for (WorkRecord workRecord : oldAll) {
            WorkRecord temp = new WorkRecord();
            UpdateUtils.copyNonNullProperties(workRecord, temp);
            String newFlowTreeId = treeIdMap.get(temp.getFlowTreeId());
            if (StringUtils.isBlank(newFlowTreeId)) {
                //旧的treeId没有对应的新treeId, 需要删除.
            } else {
                //改造数据.
                temp.setId(IdWorker.createId());
                temp.setFlowModelId(newFlowModelId);
                temp.setWorkLineId(workLineMap.get(temp.getWorkLineId()));
                temp.setFlowTreeId(newFlowTreeId);
                String flowWorkFileId = temp.getFlowWorkFileId();
                //record标识是备注的type,是没有flowWorkFileId字段的.
                if (StringUtils.isNotBlank(flowWorkFileId)) {
                    temp.setFlowWorkFileId(flowWorkFileMap.get(flowWorkFileId));
                }
                all.add(temp);
            }
        }
        //all的数据构造完成, 保存到数据库中.
        workRecordDAO.saveAll(500, all);
    }

    /**
     * 通过oldFlowModelId查询到本次更新的记录, 然后存储到history表中, 然后删除查询到的记录, 将内存中的记录更新后保存回去.
     *
     * @param oldFlowModelId
     * @param newFlowModelId
     * @param workLineMap
     */
    private void updateSpendTime(String oldFlowModelId, String newFlowModelId, Map<String, String> workLineMap) {
        List<SpendTime> oldAll = spendTimeDAO.findByFlowModelIdAndIsDelete(oldFlowModelId, Constants.STATUE_NORMAL);
        // 保存
        spendTimeHistoryDAO.saveAll(500, oldAll);
        // 刪除
        spendTimeDAO.deleteIdIn(oldAll.stream().map(SpendTime::getId).collect(Collectors.joining(",")));
        // 更新
        List<SpendTime> newAll = new ArrayList<>(oldAll.size());
        for (SpendTime spendTime : oldAll) {
            String newWorkLineId = workLineMap.get(spendTime.getWorkLineId());
            if (StringUtils.isNotBlank(newWorkLineId)) {
                spendTime.setId(IdWorker.createId());
                spendTime.setWorkLineId(newWorkLineId);
                spendTime.setFlowModelId(newFlowModelId);
                newAll.add(spendTime);
            } else {
                log.error("spendTime[id:{}, oldWorkLineId:{}]沒有找到新的workLine.", spendTime.getId(),
                        spendTime.getWorkLineId());
            }
        }
        spendTimeDAO.saveAll(500, newAll);
    }

    private Map<String, String> getTreeIdMap(List<VersionIteratorRel> versionIteratorRels) {
        Map<String, String> idMap = new HashMap<>(versionIteratorRels.size());
        versionIteratorRels.forEach(v -> idMap.put(v.getOldId(), v.getNewId()));
        return idMap;
    }

    private Map<String, FlowTree> getFlowTreeMap(List<FlowTree> flowTrees) {
        Map<String, FlowTree> flowTreeMap = new HashMap<>(flowTrees.size());
        flowTrees.forEach(f -> flowTreeMap.put(f.getId(), f));
        return flowTreeMap;
    }

    private Map<String, String> getFlowWorkFileMap(List<VersionUpdateFileRel> fileRels) {
        Map<String, String> flowWorkFileMap = new HashMap<>(fileRels.size());
        fileRels.forEach(f -> flowWorkFileMap.put(f.getOldFileId(), f.getNewFileId()));
        return flowWorkFileMap;
    }

    @Override
    public boolean initWorkLine(Map<String, String> flowTreeMap) {
        String projectId = flowTreeMap.get("projectId");
        ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
        if (null == projectInfo) {
            log.error("initWorkLine():没有查询到projectId[{}]对应的对象.", projectId);
            return false;
        }

        String sync = redisSync.getSync(projectId + "initWorkLine", 180, TimeUnit.SECONDS);
        if (StringUtils.isBlank(sync)) {
            log.info("初始化节点时未获取到锁.", sync);
            return false;
        }
        try {
            List<String> did = StringUtils.isBlank(flowTreeMap.get("did")) ? null : CommonUtils.StringToList(flowTreeMap.get("did"), ",");
            List<String> doing = StringUtils.isBlank(flowTreeMap.get("doing")) ? null : CommonUtils.StringToList(flowTreeMap.get("doing"), ",");
            List<String> skip = StringUtils.isBlank(flowTreeMap.get("skip")) ? null : CommonUtils.StringToList(flowTreeMap.get("skip"), ",");
            if (CollectionUtils.isEmpty(did)) {
                did = new ArrayList<>();
            }
            if (CollectionUtils.isEmpty(doing)) {
                doing = new ArrayList<>();
            }
            if (CollectionUtils.isEmpty(skip)) {
                skip = new ArrayList<>();
            }
            List<String> tempList = new ArrayList<>(did.size() + doing.size() + skip.size() + 1);
            Set<String> repeat = new HashSet<>(did.size() + doing.size() + skip.size() + 1);

            tempList.addAll(did);
            tempList.addAll(doing);
            tempList.addAll(skip);
            for (String id : tempList) {
                if (!repeat.add(id)) {
                    FlowTree flowTree = flowTreeRepository.findById(id).orElse(new FlowTree());
                    throw new CustomException(500, String.format("节点[%s]有重复, 请修改后提交.", flowTree.getName()));
                }
            }
            List<FlowTree> didFlowTrees = flowTreeRepository.findByIdIn(did);
            List<FlowTree> doingFlowTrees = flowTreeRepository.findByIdIn(doing);
            List<FlowTree> skipFlowTrees = flowTreeRepository.findByIdIn(skip);
            //设置已办
            converFlowTreeToWorkLine(projectInfo, didFlowTrees, Constants.WORKLINE_RESULT_DID, Constants.WORKLINE_STATUS_DID);
            //设置待办
            converFlowTreeToWorkLine(projectInfo, doingFlowTrees, Constants.WORKLINE_RESULT_DOING, Constants.WORKLINE_STATUS_DOING);
            //设置跳过
            converFlowTreeToWorkLine(projectInfo, skipFlowTrees, Constants.WORKLINE_RESULT_SKIP, Constants.WORKLINE_STATUS_DID);
            if (CollectionUtils.isNotEmpty(didFlowTrees) || CollectionUtils.isNotEmpty(doingFlowTrees) ||
            CollectionUtils.isNotEmpty(skipFlowTrees)) {
                //然后标识为已开启流程的项目.
                projectInfo.setResult(Constants.PROJECT_RESULT_DOING);
                projectInfoRepository.save(projectInfo);
            }
            return true;
        } finally {
            redisSync.removeSync(projectId + "initWorkLine", sync);
        }
    }


    private void converFlowTreeToWorkLine(ProjectInfo projectInfo, List<FlowTree> flowTrees, Integer result, Integer status) {
        if (CollectionUtils.isEmpty(flowTrees)) {
            return;
        }
        String didGroups = IdWorker.createId();
        ArrayList<WorkLine> workLines = new ArrayList<>(flowTrees.size());
        for (FlowTree flowTree : flowTrees) {
            //过滤掉菱形节点
            if (!Constants.PROJECT_GRADE_NONE.equals(flowTree.getGrade())) {
                continue;
            }
            FlowTree temp = new FlowTree();
            UpdateUtils.copyNonNullProperties(flowTree, temp);
            if (null == temp.getType() || Constants.TODO_JIEBAN.equals(temp.getType())) {
                // 判断该节点是否是街办办理.
                String streetOffice = projectInfo.getStreetOffice();
                CheckUtil.isBlank(streetOffice, 500, Constants.JIEBAN_IS_NULL);
                temp.setDeptIds(streetOffice);
                String unitId = deptRepository.findById(streetOffice).orElse(new Department()).getParentId();
                temp.setUnitIds(Constants.DEPT_PARENT_ID.equals(unitId) ? temp.getDeptIds() : unitId);
            } else if (null == temp.getType() || Constants.TODO_ZERENKESHI.equals(temp.getType())) {
                // 判断该节点是否是责任科室处理.
                String responsibleDept = projectInfo.getResponsibleDept();
                CheckUtil.isBlank(responsibleDept, 500, Constants.ZERENKESHI_IS_NULL);
                temp.setDeptIds(responsibleDept);
                String unitId = deptRepository.findById(responsibleDept).orElse(new Department()).getParentId();
                temp.setUnitIds(Constants.DEPT_PARENT_ID.equals(unitId) ? temp.getDeptIds() : unitId);
            }

            WorkLine workLine = CreateObjUtils.create(WorkLine.class);
            workLine.converFlowTreeToWorkLine(temp);
            workLine.setNum(temp.getNum());
            workLine.setProjectId(projectInfo.getId());
            workLine.setType(projectInfo.getType());
            workLine.setStatus(status);
            workLine.setResult(result);
            workLine.setRemark("");
            workLine.setOperatorName(UserSession.getUserSession().getName());
            //是已办, 那么就全在一个分组下. 是待办, 各自是各自的分组.
            if (Constants.WORKLINE_STATUS_DID.equals(workLine.getStatus())) {
                workLine.setGroups(didGroups);
            } else {
                workLine.setGroups(IdWorker.createId());
            }
            workLines.add(workLine);
        }
        workLineDAO.saveAll(300, workLines);
    }

    @Override
    public boolean deleteWorkLine(String projectId) {
        Assert.notNull(projectId, "projectId不能为null");
        boolean delete = workLineDAO.deleteByProjectId(projectId);
        if (delete) {
            ProjectInfo projectInfo = projectInfoRepository.findById(projectId).orElse(null);
            if (null == projectInfo) {
                log.error("projectId[{}]没有找到对象.", projectId);
                return false;
            }
            projectInfo.setResult(Constants.WORKLINE_STATUS_WAITDO);
            projectInfoRepository.save(projectInfo);
        }
        return delete;
    }
}
