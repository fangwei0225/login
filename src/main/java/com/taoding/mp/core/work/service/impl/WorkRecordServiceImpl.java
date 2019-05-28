package com.taoding.mp.core.work.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.FlowWorkFileRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.flow.service.FlowWorkFileService;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.dao.WorkRecordRepository;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.service.WorkRecordService;
import com.taoding.mp.core.work.vo.AffixVO;
import com.taoding.mp.core.work.vo.FlowWorkFileVO;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.CreateObjUtils;
import com.taoding.mp.util.RedisSync;
import com.taoding.mp.util.UpdateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description:
 */
@Service
@Slf4j
public class WorkRecordServiceImpl implements WorkRecordService {

    @Autowired
    WorkRecordRepository workRecordRepository;
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    FlowWorkFileService flowWorkFileService;
    @Autowired
    FlowTreeRepository flowTreeRepository;
    @Autowired
    FlowWorkFileRepository flowWorkFileRepository;
    @Autowired
    RedisSync redisSync;

    @Override
    public WorkRecord saveWorkRecord(WorkRecord workRecord) {
        FlowTree flowTree = flowTreeRepository.findById(workRecord.getFlowTreeId()).orElse(null);
        if (null == flowTree) {
            log.error("通过flowTreeId[{}]没有找到对象.", workRecord.getFlowTreeId());
            return null;
        }
        if(StringUtils.isBlank(workRecord.getProjectId())){
            WorkLine workLine = workLineRepository.findByIdAndIsDelete(workRecord.getWorkLineId(), Constants.STATUE_NORMAL).orElse(new WorkLine());
            workRecord.setProjectId(workLine.getProjectId());
            if(StringUtils.isBlank(workRecord.getProjectId())){
                log.error("添加附件或者备注的时候, 没有携带projectId.");
            }
        }
        if(StringUtils.isBlank(workRecord.getWorkLineId())){
            //在plan B中, 可能提交附件这些的时候还没创建workLine节点, 所以在这里初始化.
            WorkLine workLine = CreateObjUtils.create(WorkLine.class);
            workLine.setFlowTreeId(workRecord.getFlowTreeId());
            workLine.setProjectId(workLine.getProjectId());
            workLine.setStatus(Constants.WORKLINE_STATUS_DOING);
            //设置为这样, 在查询办理进度的时候会过滤掉这种节点.设置为未办理.
            workLine.setResult(Constants.WORKLINE_RESULT_DOING);
            workLineRepository.save(workLine);
        }
        String sync = redisSync.getSync(workRecord.getWorkLineId() + workRecord.getFlowWorkFileId(),
                180, TimeUnit.SECONDS);
        if (StringUtils.isBlank(sync)) {
            log.error("用户[{}]申请锁失败, 操作的是workLine[{}]", UserSession.getUserSession().getName(),
                    workRecord.getWorkLineId());
            return new WorkRecord();
        }
        try {
            String flowModeId = flowTree.getFlowModeId();
            workRecord.setFlowModelId(flowModeId);
            workRecord.setOperatorName(UserSession.getUserSession().getName());
            WorkRecord dataBase;
            //区分添加还是更新.
            if (StringUtils.isBlank(workRecord.getId())) {
                dataBase = addWorkRecord(workRecord);
            } else {
                dataBase = updateWorkRecord(workRecord);
            }
            return dataBase;
        } finally {
            redisSync.removeSync(workRecord.getWorkLineId() + workRecord.getFlowWorkFileId(), sync);
        }
    }

    /**
     * 执行真正的添加
     * 需要 workLineId, flowWorkFileId
     */
    private WorkRecord addWorkRecord(WorkRecord workRecord) {
        Integer type = workRecord.getType();
        if (null == type) {
            type = 0;
        }
        if (type == 0) {
            //添加的是备注
            workRecord = CreateObjUtils.addBase(workRecord);
            // 备注不需要flowWorkFileId
            if (StringUtils.isAnyBlank(workRecord.getWorkLineId(), workRecord.getFlowTreeId())) {
                log.error("id:{}的workLineId or flowTreeId is null.");
                return null;
            }
            return workRecordRepository.save(workRecord);
        } else {
            //添加的是申报材料
            String flowWorkFileId = workRecord.getFlowWorkFileId();
            List<WorkRecord> records = workRecordRepository.findByFlowWorkFileIdAndIsDelete(flowWorkFileId, Constants.STATUE_NORMAL);
            if (records == null || records.isEmpty()) {
                //数据库没有记录, 添加新的
                workRecord = CreateObjUtils.addBase(workRecord);
                if (StringUtils.isAnyBlank(workRecord.getWorkLineId(), workRecord.getFlowTreeId(), workRecord.getFlowWorkFileId())) {
                    log.error("id:{}的workLineId or flowTreeId or flowWorkFileId is null.");
                    return null;
                }
                //将申报材料不同类别的名称记录下来.
                FlowWorkFile flowWorkFile = flowWorkFileRepository.findById(flowWorkFileId).orElse(new FlowWorkFile());
                workRecord.setFlowWorkFileName(flowWorkFile.getName());
                if (workRecord.getAffixList() != null && !workRecord.getAffixList().isEmpty()) {
                    workRecord.setAffix(JSONArray.toJSONString(workRecord.getAffixList()));
                }
                workRecord.setUpdateTime(CommonUtils.getStringDate(new Date()));
                if (workRecord.getStatus() == null) {
                    workRecord.setStatus(0);
                }
                return workRecordRepository.save(workRecord);
            } else {
                //数据库已有存在的记录
                UpdateUtils.copyNonNullProperties(records.get(records.size() - 1), workRecord);
                if (workRecord.getAffixList() != null && !workRecord.getAffixList().isEmpty()) {
                    workRecord.setAffix(JSONArray.toJSONString(workRecord.getAffixList()));
                }
                workRecord.setUpdateTime(CommonUtils.getStringDate(new Date()));
                return workRecordRepository.save(workRecord);
            }
        }
    }

    /**
     * 执行真正的修改/删除
     */
    private WorkRecord updateWorkRecord(WorkRecord workRecord) {
        WorkRecord byId = workRecordRepository.findById(workRecord.getId()).orElse(null);
        if (null == byId) {
            return new WorkRecord();
        }
        UpdateUtils.copyNonNullProperties(byId, workRecord);
        if (workRecord.getAffixList() != null && !workRecord.getAffixList().isEmpty()) {
            workRecord.setAffix(JSONArray.toJSONString(workRecord.getAffixList()));
        }
        workRecord.setUpdateTime(CommonUtils.getStringDate(new Date()));
        WorkRecord save = workRecordRepository.save(workRecord);
        if (workRecord.getAffixList() == null || workRecord.getAffixList().isEmpty()) {
            save.setAffixList(JSONObject.parseArray(save.getAffix(), AffixVO.class));
        }
        return save;
    }

    @Override
    public List<FlowWorkFileVO> getFlowWorkFileVOByWorkLineId(String workLineId) {
        WorkLine workLine = workLineRepository.findByIdAndIsDelete(workLineId, Constants.STATUE_NORMAL).orElse(null);
        if (null == workLine) {
            log.error("WorkRecordServiceImpl.getFlowWorkFileVOByWorkLineId()通过workLineId{}没有查到对象.", workLineId);
        }

        // 通过workLine查询到对应的flowtrees, 拿到该节点所有的申报材料信息行(预先定义的)
        List<FlowWorkFile> flowWorkFiles = flowWorkFileService.selectByFlowTreeId(workLine.getFlowTreeId(), 1);
        if (CollectionUtils.isEmpty(flowWorkFiles)) {
            //没有附带的申报材料, 直接退出.
            return new ArrayList<>();
        }
        List<FlowWorkFileVO> flowWorkFileVOS = new ArrayList<>(flowWorkFiles.size());
        //查询已经生成了的申报材料记录
        List<WorkRecord> byExist = workRecordRepository.findByWorkLineIdAndTypeAndIsDeleteOrderByCreateTimeDesc(workLineId, 1, 1);
        //若没有已生成的记录, 则直接返回申报材料的名称等信息.
        if (null == byExist || byExist.isEmpty()) {
            flowWorkFiles.forEach(flowWorkFile -> {
                FlowWorkFileVO vo = new FlowWorkFileVO();
                vo.conver(flowWorkFile);
                vo.setWorkLineId(workLineId);
                vo.setStauts(0);
                flowWorkFileVOS.add(vo);
            });
            return flowWorkFileVOS;
        } else {
            //有已生成的记录
            Map<String, WorkRecord> recordMap = new HashMap<>(byExist.size());
            byExist.forEach(w -> {
                if (StringUtils.isNotBlank(w.getAffix())) {
                    w.setAffixList(JSONObject.parseArray(w.getAffix(), AffixVO.class));
                }
                recordMap.put(w.getFlowWorkFileId(), w);
            });
            flowWorkFiles.forEach(f -> {
                FlowWorkFileVO vo = new FlowWorkFileVO();
                UpdateUtils.copyNonNullProperties(f, vo);
                WorkRecord workRecord = recordMap.get(vo.getId());
                if (null == workRecord) {
                    vo.setStauts(0);
                } else {
                    vo.setStauts(workRecord.getStatus() == null ? 0 : workRecord.getStatus());
                }
                vo.setFlowWorkFileId(vo.getId());
                vo.setWorkRecord(workRecord);
                vo.setWorkLineId(workLineId);
                flowWorkFileVOS.add(vo);
            });
            return flowWorkFileVOS;
        }
    }

    @Override
    public List<WorkRecord> findByWorkLineIdAndType(String workLineId, Integer type) {
        List<WorkRecord> remarkList = workRecordRepository.findByWorkLineIdAndTypeAndIsDeleteOrderByCreateTimeDesc(workLineId, type, Constants.STATUE_NORMAL);
        return remarkList;
    }

    @Override
    public void deleteWorkRecordFromWorkLineList(List<String> workLineIdList) {
        List<WorkRecord> workRecords = workRecordRepository.findByWorkLineIdInAndIsDelete(workLineIdList, Constants.STATUE_NORMAL);
        workRecords.forEach(workRecord -> workRecord.setIsDelete(Constants.STATUE_DEL));
        workRecordRepository.saveAll(workRecords);
    }
}
