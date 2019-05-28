package com.taoding.mp.core.flow.controller;

import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.flow.service.FlowWorkFileService;
import com.taoding.mp.core.flow.vo.FileUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/17 001710:41
 */
@RestController
@RequestMapping("/server/workerFile")
public class FlowWorkFileController extends ResponseBuilder {

    @Autowired
    private FlowWorkFileService flowWorkFileService;

    @PostMapping("/update")
    public Object update(@RequestBody FileUpdateVO vo){
          return success(flowWorkFileService.update(vo));

    }

    @GetMapping("/list")
    public Object list(@RequestParam("flowTreeId") String flowTreeId){
        return success(flowWorkFileService.selectByFlowTreeId(flowTreeId, Constants.STATUE_NORMAL));
    }

    @GetMapping("/listByTopId")
    public Object fileListByTopId(@RequestParam("topId") String topId){
       return success(flowWorkFileService.fileListByTopId(topId));
    }

}
