package com.taoding.mp.core.datetime.controller;

import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.datetime.VO.DateAddVO;
import com.taoding.mp.core.datetime.service.KalendarService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/7 000714:59
 */
@RestController
@RequestMapping("/server/date")
public class KalendarController  extends ResponseBuilder {

    @Autowired
    private KalendarService kalendarService;

    @PostMapping("/add")
    public  Object add(@RequestBody DateAddVO vo){
        if (StringUtils.isBlank(vo.getDates())){
            return error("参数不能为空！");
        }
        return success(kalendarService.add(vo.getYear(),vo.getDates()));
    }

    @GetMapping("/list")
    public Object list(@RequestParam("year") Integer year){
       return success(kalendarService.list(year));
    }

}
