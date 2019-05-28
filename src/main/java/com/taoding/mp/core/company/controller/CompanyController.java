package com.taoding.mp.core.company.controller;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.core.company.entity.Company;
import com.taoding.mp.core.company.service.CompanyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 企业信息（项目单位）管理
 * @author wuwentan
 * @date 2019/5/7
 */
@RestController
@RequestMapping("/server/company")
public class CompanyController {

    @Autowired
    CompanyService companyService;

    @RequestMapping("/page")
    public ResponseVO<PageVO<Company>> getPage(@RequestBody Map<String,String> params){
        PageVO<Company> page = companyService.getPage(params);
        return new ResponseVO<>(page);
    }

    @RequestMapping("/save")
    public ResponseVO<Company> save(@RequestBody Company company){
        company = companyService.save(company);
        return new ResponseVO<>(company);
    }

    @RequestMapping("/info")
    public ResponseVO<Company> info(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            Company company = companyService.getById(id);
            return new ResponseVO<>(company);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    @RequestMapping("/delete")
    public ResponseVO<String> delete(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            companyService.deleteById(id);
            return new ResponseVO<>("操作成功");
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }
}
