package com.taoding.mp.core.flow.controller;

import com.taoding.mp.base.model.BaseEntity;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.CustomHttpStatus;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.core.flow.entity.FlowModel;
import com.taoding.mp.core.flow.service.FlowModelService;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.vo.FlowModelAddVO;
import com.taoding.mp.core.flow.vo.FlowModelUpdateVO;
import com.taoding.mp.core.flow.vo.ModelListVO;
import com.taoding.mp.core.flow.vo.VersionUpdateVO;
import com.taoding.mp.util.CreateObjUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author liuxinghong
 * @Description: 审批流程模板controller
 * @date 2019/4/15 001513:52
 */
@RestController
@RequestMapping("/server/flowModel")
public class FlowModelController extends ResponseBuilder {

    @Autowired
    private FlowModelService flowModelService;
    @Autowired
    private FlowTreeService flowTreeService;
    /**
     * 添加流程模板
     * @return
     */
    @PostMapping("/add")
    public Object add(@Validated @RequestBody FlowModelAddVO vo){

        //判断该类型下是否存在发布和未发布的版本存在
        Boolean bool = flowModelService.versionExist(vo.getType());
        if (!bool){
            throw new CustomException(CustomHttpStatus.AVE_FAIL.value(),"存在发布或者未发布的版本，无法新增");
        }
        FlowModel flowModel = new FlowModel();
        BeanUtils.copyProperties(vo,flowModel);
        BeanUtils.copyProperties(CreateObjUtils.addBase(new BaseEntity()),flowModel);
        flowModel.setIsEffect(Constants.VERSION_EFFECT_NO);//初始化的末班状态为 未发布
        flowModel.setIsLatest(Constants.VERSION_ISRELASR_FALSE);
        return success(flowModelService.add(flowModel));
    }


    /**
     * 修改流程模板
     * @return
     */
    @PostMapping("/update")
    public Object update(@Validated @RequestBody FlowModelUpdateVO vo){
        if (null != flowModelService.update(vo)){
            return success();
        }
        return error("更新的数据不存在！");
    }

    /**
     * 删除流程模板
     * @return
     */
    @GetMapping("/delete")
    public Object delete(@RequestParam("id") String id){
       Boolean bool= flowModelService.delete(id);
       if (bool){
           return success("删除成功");
       }
      return error("删除失败");
    }

    /**
     * 列表
     * @return
     */
    @PostMapping ("/list")
    public Object list(  @RequestBody ModelListVO vo){
        return success(
                flowModelService.findAll(vo));

    }

    /**
     * 创建下一个流程版本
     * @return
     */
    @PostMapping("/createNewVersion")
    public Object updateVersion(@Validated @RequestBody VersionUpdateVO vo ){
        boolean b = flowTreeService.updateVersion(vo);
        if (b){
            return success("创建成功");
        }
        return error("创建失败");
    }

    /**
     * 发行版本
     * @return
     */
    @GetMapping("/released")
    public Object releasedVersion(@RequestParam("modeId") String modelId){
        return success(flowModelService.released(modelId));
    }

}
