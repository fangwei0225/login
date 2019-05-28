package com.taoding.mp.base.controller;

import com.taoding.mp.base.model.ConfigModel;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置接口类
 *
 * @author wuwentan
 * @date 2018/8/9
 */
@RestController
@RequestMapping("/server/config")
public class ConfigController {

    @Autowired
    ConfigService configService;

    @RequestMapping("/list")
    public ResponseVO list(@RequestParam(value = "app", required = false, defaultValue = "") String app) {
        return new ResponseVO(configService.getList(app));
    }

    @RequestMapping("/get")
    public ResponseVO info(@RequestBody ConfigModel config) {
        return new ResponseVO(configService.getConfig(config.getKey()));
    }

    @RequestMapping("/set")
    public ResponseVO save(@RequestBody ConfigModel config) {
        if (config != null && StringUtils.isNotBlank(config.getKey()) && StringUtils.isNotBlank(config.getValue())) {
            configService.setConfig(config.getKey(), config.getValue());
            return new ResponseVO("");
        }
        return new ResponseVO(400, "配置参数或参数值不能为空");
    }

    /**
     * 删除
     *
     * @param config
     * @return
     * @author youngsapling
     */
    @RequestMapping("/delete")
    public ResponseVO delete(@RequestBody ConfigModel config) {
        return new ResponseVO("");
    }
}
