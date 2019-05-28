package com.taoding.mp.core.work.controller;

import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.work.dao.SpendTimeRepository;
import com.taoding.mp.core.work.dao.WorkLineRepository;
import com.taoding.mp.core.work.entity.SpendTime;
import com.taoding.mp.core.work.entity.WorkLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: youngsapling
 * @date: 2019-05-21
 * @modifyTime:
 * @description: 用于给刘婷测试使用.
 */
@RestController
@RequestMapping("/test")
public class TestTimeController {
    @Autowired
    WorkLineRepository workLineRepository;
    @Autowired
    SpendTimeRepository spendTimeRepository;

    @RequestMapping("/get")
    public ResponseVO getWorkLine(@RequestParam String id){
        WorkLine workLine = workLineRepository.findByIdAndIsDelete(id, Constants.STATUE_NORMAL).orElse(null);
        if(null == workLine){
            return ResponseBuilder.error("", id + "没有查询到正确的数据.");
        }else {
            return ResponseBuilder.success(workLine);
        }
    }

    @RequestMapping("/update")
    public ResponseVO update(@RequestParam String id, @RequestParam String createTime, @RequestParam String updateTime) {
        WorkLine workLine = workLineRepository.findByIdAndIsDelete(id, Constants.STATUE_NORMAL).orElse(null);
        if (null == workLine) {
            return ResponseBuilder.error("", id + "没有查询到正确的数据.");
        }
        workLine.setCreateTime(createTime);
        workLine.setUpdateTime(updateTime);
        workLineRepository.save(workLine);
        return ResponseBuilder.success(workLine);
    }

    @RequestMapping("/getSpend")
    public ResponseVO getSpend(@RequestParam String workLineId){
        SpendTime spendTime = spendTimeRepository.findByWorkLineIdAndIsDelete(workLineId, Constants.STATUE_NORMAL).orElse(null);
        if(null == spendTime){
            return ResponseBuilder.error("", workLineId + "没有查询到正确的数据.");
        }else {
            return ResponseBuilder.success(spendTime);
        }
    }

    @RequestMapping("/updateSpend")
    public ResponseVO updateSpend(@RequestParam String workLineId, @RequestParam String createTime, @RequestParam String updateTime) {
        SpendTime spendTime = spendTimeRepository.findByWorkLineIdAndIsDelete(workLineId, Constants.STATUE_NORMAL).orElse(null);
        if(null == spendTime){
            return ResponseBuilder.error("没找到对象.");
        }
        spendTime.setCreateTime(createTime);
        spendTime.setUpdateTime(updateTime);
        spendTimeRepository.save(spendTime);
        return ResponseBuilder.success(spendTime);
    }
}
