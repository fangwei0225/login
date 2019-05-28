package com.taoding.mp.core.project.controller;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.project.service.ProjectInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目信息管理接口
 * @author wuwentan
 * @date 2019/4/15
 */
@RestController
@RequestMapping("/server/project")
public class ProjectInfoController {

    @Autowired
    ProjectInfoService projectInfoService;

    /**
     * 分页查询项目列表
     * @param params
     * @return
     */
    @PostMapping("/page")
    public ResponseVO<PageVO<ProjectInfo>> getPage(@RequestBody Map<String, String> params){
        PageVO<ProjectInfo> pageVO = projectInfoService.getPage(params);
        return new ResponseVO<>(pageVO);
    }

    /**
     * 新增、修改项目信息
     * @param projectInfo
     * @return
     */
    @PostMapping("/save")
    public ResponseVO<ProjectInfo> save(@RequestBody ProjectInfo projectInfo){
        projectInfo = projectInfoService.save(projectInfo);
        return new ResponseVO<>(projectInfo);
    }

    /**
     * 校验项目代码是否重复
     * @param code
     * @return
     */
    @PostMapping("/checkCode")
    public ResponseVO<ProjectInfo> checkCode(@RequestParam(required = false) String code){
        ProjectInfo projectInfo = projectInfoService.checkProjectByCode(code);
        return new ResponseVO<>(projectInfo);
    }

    /**
     * 根据id查询项目信息
     * @param id
     * @return
     */
    @PostMapping("/info")
    public ResponseVO<ProjectInfo> info(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            ProjectInfo projectInfo = projectInfoService.getById(id);
            return new ResponseVO<>(projectInfo);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 根据id删除项目信息
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResponseVO<String> delete(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            projectInfoService.deleteById(id);
            return new ResponseVO<>("操作成功");
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 领导关注-项目列表分页
     * @param params
     * @return
     */
    @PostMapping("/leaderPage")
    public ResponseVO<PageVO<ProjectInfo>> leaderPage(@RequestBody Map<String, String> params){
        PageVO<ProjectInfo> pageVO = projectInfoService.leaderPage(params);
        return new ResponseVO<>(pageVO);
    }

    /**
     * 根据项目状态统计数量
     * @return
     */
    @PostMapping("/count")
    public ResponseVO<Map<String,Object>> count(){
        Map<String,Object> count = projectInfoService.countByStatus();
        return new ResponseVO<>(count);
    }

    /**
     * 企业用户-我的项目分页列表
     * @param params
     * @return
     */
    @PostMapping("/companyPage")
    public ResponseVO<PageVO<ProjectInfo>> companyPage(@RequestBody Map<String, String> params){
        PageVO<ProjectInfo> pageVO = projectInfoService.companyPage(params);
        return new ResponseVO<>(pageVO);
    }
}
