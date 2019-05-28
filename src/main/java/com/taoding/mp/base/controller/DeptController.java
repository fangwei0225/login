package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.DeptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门管理
 * @author wuwentan
 * @date 2019/4/11
 */
@RestController
@RequestMapping("/server/dept")
public class DeptController {

    @Autowired
    DeptService deptService;

    /**
     * 查询所有部门（根据序号倒排）
     * @return
     */
    @PostMapping(value = "/listAll")
    public ResponseVO<List<Department>> listAll(){
        List<Department> list = deptService.listAll();
        return new ResponseVO<>(list);
    }

    /**
     * 分页查询部门列表（根据序号倒排）
     * @param params
     * @return
     */
    @PostMapping(value = "/page")
    public ResponseVO<PageVO<Department>> getPage(@RequestBody Map<String, String> params){
        PageVO<Department> page = deptService.getPage(params);
        return new ResponseVO<>(page);
    }

    /**
     * 新增或修改部门信息
     * @param department
     * @return
     */
    @PostMapping(value = "/save")
    public ResponseVO<Department> save(@RequestBody Department department) {
        Department save = deptService.save(department);
        return new ResponseVO(save);
    }

    /**
     * 根据id查询部门信息
     * @param id
     * @return
     */
    @PostMapping("/info")
    public ResponseVO<Department> info(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            Department dept = deptService.getById(id);
            return new ResponseVO<>(dept);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 根据id删除部门信息
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResponseVO<String> delete(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            deptService.deleteById(id);
            return new ResponseVO<>("操作成功");
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 根据parentId查询下一级部门列表
     * @param pid
     * @return
     */
    @PostMapping("/listByPid")
    public ResponseVO<List<Department>> listByPid(@RequestParam(required = false,defaultValue = "0") String pid){
        List<Department> list = deptService.listByPid(pid);
        return new ResponseVO<>(list);
    }

}
