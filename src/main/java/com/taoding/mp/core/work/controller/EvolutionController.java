package com.taoding.mp.core.work.controller;

import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.work.service.EvolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: youngsapling
 * @date: 2019-05-06
 * @modifyTime:
 * @description:
 */
@RestController
@RequestMapping("/server/workLine")
public class EvolutionController {
    @Autowired
    EvolutionService evolutionService;

    @PostMapping(value = "/initWorkLine")
    public ResponseVO initWorkLine(@RequestBody Map<String, String> map){
        boolean temp = evolutionService.initWorkLine(map);
        return ResponseBuilder.success(temp);
    }

    @PostMapping(value = "/delete")
    public ResponseVO deleteWorkLine(@RequestBody Map<String, String> map){
        boolean temp = evolutionService.deleteWorkLine(map.get("projectId"));
        return ResponseBuilder.success(temp);
    }
}
