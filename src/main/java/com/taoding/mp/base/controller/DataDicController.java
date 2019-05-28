package com.taoding.mp.base.controller;

import com.taoding.mp.base.entity.DataDictionary;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.base.service.DataDicService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典管理
 * @author wuwentan
 * @date 2019/3/7
 */
@RestController
@RequestMapping("/server/dataDic")
public class DataDicController {

    @Autowired
    DataDicService dataDicService;

    /**
     * 分页查询数据字典列表
     * @return
     */
    @PostMapping("/page")
    public ResponseVO<PageVO<DataDictionary>> page(@RequestBody Map<String, String> params){
        PageVO<DataDictionary> pageVO = dataDicService.page(params);
        return new ResponseVO<>(pageVO);
    }

    /**
     * 新增、修改数据字典
     * @param dd
     * @return
     */
    @PostMapping("/save")
    public ResponseVO<DataDictionary> save(@RequestBody DataDictionary dd){
        return new ResponseVO<>(dataDicService.save(dd));
    }

    /**
     * 根据id查询数据字典信息
     * @param id
     * @return
     */
    @RequestMapping("/info")
    public ResponseVO<DataDictionary> info(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            DataDictionary dd = dataDicService.getById(id);
            return new ResponseVO<>(dd);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 删除数据字典
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResponseVO deleteById(@RequestParam(required = false) String id){
        if(StringUtils.isNotBlank(id)){
            dataDicService.deleteById(id);
            return new ResponseVO<>("操作成功");
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数id");
        }
    }

    /**
     * 根据type和parentId查询字典列表
     * @return
     */
    @PostMapping("/listByType")
    public ResponseVO<List<Map<String,Object>>> listByType(@RequestParam(required = false) String type,
                                                           @RequestParam(required = false, defaultValue = "0") String parentId){
        if(StringUtils.isNotBlank(type)){
            List<Map<String,Object>> list = dataDicService.list(type, parentId);
            return new ResponseVO<>(list);
        }else{
            return new ResponseVO<>(400,"参数错误：请传递参数type");
        }
    }

    /**
     * 字典数据树形列表
     * @return
     */
    @PostMapping("/treeList")
    public ResponseVO<List<DataDictionary>> treeList(@RequestParam(required = false) String name,
                                                     @RequestParam(required = false) String type){
        List<DataDictionary> treeList = dataDicService.treeList(name, type);
        return new ResponseVO<>(treeList);
    }
}
