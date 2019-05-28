package com.taoding.mp.core.work.controller;

import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.core.work.service.WorkRecordService;
import com.taoding.mp.core.work.vo.FlowWorkFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author: youngsapling
 * @date: 2019-04-18
 * @modifyTime:
 * @description:
 */
@RestController
@RequestMapping("/server/record")
public class WorkRecordController {

    @Autowired
    WorkRecordService workRecordService;

    /**
     * 保存
     * @param workRecord
     * @return
     */
    @RequestMapping("/save")
    public ResponseVO save(@RequestBody WorkRecord workRecord){
        WorkRecord dateBase = workRecordService.saveWorkRecord(workRecord);
        return ResponseBuilder.success(dateBase);
    }

    /**
     * [申报材料]
     */
    @RequestMapping("/get")
    public ResponseVO<List<FlowWorkFileVO>> get(@RequestBody Map<String, String> map){
        String workLineId = map.get("workLineId");
        List<FlowWorkFileVO> byWorkLineId = workRecordService.getFlowWorkFileVOByWorkLineId(workLineId);
        return ResponseBuilder.success(byWorkLineId);
    }
}
