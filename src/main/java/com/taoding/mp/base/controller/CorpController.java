package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Corp;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.CorpService;
import com.taoding.mp.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 企业信息管理
 *
 * @author wuwentan
 * @date 2018/11/7
 */
@RestController
@RequestMapping("/server/corp")
public class CorpController {

    @Autowired
    CorpService corpService;

    /**
     * 分页查询企业信息列表
     *
     * @param params
     * @return
     */
    @PostMapping("/page")
    public ResponseVO<PageVO<Corp>> page(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(corpService.getPage(params));
    }

    /**
     * 查询企业信息列表
     *
     * @param params
     * @return
     */
    @PostMapping("/list")
    public ResponseVO<List<Corp>> list(@RequestBody Map<String, String> params) {
        return new ResponseVO<>(corpService.getList(params));
    }

    /**
     * 根据id查询企业信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/info")
    public ResponseVO<Corp> info(@RequestParam String id) {
        return new ResponseVO<>(corpService.getById(id));
    }

    /**
     * 检查corpId是否可用
     *
     * @param corpId
     * @return
     */
    @PostMapping("/checkCorpId")
    public ResponseVO checkCorpId(@RequestParam String corpId) {
        Corp corp = corpService.getById(corpId);
        if (corp != null) {
            return new ResponseVO(403, "该企业标识已被使用");
        }
        return new ResponseVO("");
    }

    /**
     * 新增企业信息
     *
     * @param corp
     * @return
     */
    @PostMapping("/add")
    public ResponseVO<Corp> add(@RequestBody Corp corp) {
        corp.setStatus(1);
        corp.setCreateTime(CommonUtils.getStringDate(new Date()));
        corp.setOperator(UserSession.getUserSession().getUserId());
        return new ResponseVO<>(corpService.save(corp));
    }

    /**
     * 修改企业信息
     *
     * @param corp
     * @return
     */
    @PostMapping("/update")
    public ResponseVO<Corp> update(@RequestBody Corp corp) {
        Corp corp_old = corpService.getById(corp.getCorpId());
        if (corp_old != null) {
            corp_old.setName(corp.getName());
            corp_old.setStatus(corp.getStatus());
            corp_old.setOperator(UserSession.getUserSession().getUserId());
            return new ResponseVO<>(corpService.save(corp));
        }
        return new ResponseVO<>(500, "企业信息不存在");
    }

    /**
     * 逻辑删除企业信息
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResponseVO delete(@RequestParam String id) {
        corpService.deleteById(id);
        return new ResponseVO("");
    }
}
