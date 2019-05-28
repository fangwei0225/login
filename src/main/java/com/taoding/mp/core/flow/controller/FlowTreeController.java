package com.taoding.mp.core.flow.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.ResponseBuilder;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.service.FlowWorkFileService;
import com.taoding.mp.core.flow.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuxinghong
 * @Description: 模板树
 * @date 2019/4/15 001517:08
 */
@RestController
@RequestMapping("/server/flowTree")
public class FlowTreeController extends ResponseBuilder {

    @Autowired
    private FlowTreeService flowTreeService;
    @Autowired
    private FlowWorkFileService flowWorkFileService;

    /**
     *初始化流程节点添加(废弃)
     * @param vo
     * @return
     */
/**
    @PostMapping("/add")
    public Object add(@Validated @RequestBody FlowTreeAddVO vo){
        FlowTree flowTree = CreateObjUtils.create(FlowTree.class);
        BeanUtils.copyProperties(vo,flowTree);
      //  flowTree.setCode(IdWorker.createId());
        ArrayList<FlowWorkFile> files = Lists.newArrayList();
        if (StringUtils.isNotBlank(vo.getFileName())){
            flowTree.setHasFlowWorkFile(Constants.FLOW_FILE_OK);
            List<String> fileList = Splitter.on(",").trimResults().splitToList(vo.getFileName());
            fileList.forEach(item->{
                FlowWorkFile workFile = CreateObjUtils.create(FlowWorkFile.class);
                workFile.setFlowTreeId(flowTree.getId());
              //  workFile.setCode(IdWorker.createId());
                workFile.setName(item);
                files.add(workFile);
            });
        }else {
            flowTree.setHasFlowWorkFile(Constants.FLOW_FILE_NO);
        }
        //获取上一级节点列表
        List<FlowTree> treeList= flowTreeService.selectSuperList(flowTree.getParentId());
        if (CollectionUtils.isNotEmpty(treeList)){
            //todo：获取原来结构的下一级节点数据（暂时不支持从流程中间添加节点）
//            List<FlowTree> nextList=flowTreeService.selectNextListByPId(flowTree.getParentId());
//            if (CollectionUtils.isNotEmpty(nextList)){
//                //维护上下节点的关系
//            }
            //设置路径
            flowTree.setPathIds(getPathIds(flowTree,treeList));
            //设置上级的子级个数
            setSuperChildNum(treeList,flowTree);
            flowTreeService.updateSuperList(treeList);
        }else {
            flowTree.setPathIds(flowTree.getId());
        }
        flowTree.setHasChild(Constants.TOP_PARENT);//默认子级填0
        FlowTree tree = flowTreeService.add(flowTree, files);
        if (null!=tree){
            return success(tree);
        }
        return error("添加失败！");
    }

**/
    /**
     * 修改模板节点(只能修改名称)
     * @return
     */
    @PostMapping("/update")
    public Object update(@Validated @RequestBody FlowTreeUpdateVO vo){
        FlowTree flowTree = flowTreeService.selectById(vo.getId());
        BeanUtils.copyProperties(vo,flowTree);
        flowTree.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
        if (StringUtils.isNotBlank(vo.getFileName())){
            flowTree.setHasFlowWorkFile(Constants.FLOW_FILE_OK);
        }else {
            flowTree.setHasFlowWorkFile(Constants.FLOW_FILE_NO);
        }
        FlowTree save = flowTreeService.save(flowTree);
        //更新审批材料
        flowWorkFileService.update(new FileUpdateVO(vo.getId(),vo.getFileName()));
        if (null !=save){
            return success(save);
        }
        return error("更新失败！");
    }

    @GetMapping("/detail")
    public Object detail(@RequestParam("id") String id){

        return  success(flowTreeService.detail(id));
    }

    /**
     * 获取不同level级别list列表
     * @return
     */
    @PostMapping("/pageList")
    public Object pageList(@Validated @RequestBody FlowTreePageListVO vo){
       return success(flowTreeService.pageList(vo));
    }



    /**
     * web端获取预览图
     * @return
     */
    @GetMapping("/view")
    public Object getPreview(@RequestParam("flowModeId") String flowModelId,@RequestParam("level") Integer level){
       return success(flowTreeService.view(flowModelId,level));
    }

    /**
     * 添加项目时根据项目类型查询流程节点预览
     * @return
     */
    @GetMapping("/projectView")
    public Object addProjectView(@RequestParam("type") String type,@RequestParam("level") Integer level){
        return success(flowTreeService.viewByApp(type,level));
    }

    /**
     * 创建新模板流程时添加节点插入接口
     * @return
     */
    @PostMapping("/insertNewNode")
    public Object insertNewVersionNode(@Validated @RequestBody InsertNewNodeVO vo){
        return success(flowTreeService.addNewVersionNode(vo));
    }


    /**
     * 根据当前节点ids 获取level级别的所有下级节点数据(web)
     * @return
     */
    @GetMapping("/getChild")
    public Object getAllChildrenById(@RequestParam("flowTreeIds") String flowTreeIds
            ,@RequestParam("level") Integer level,@RequestParam("flowModeId") String flowModelId,@RequestParam("topId") String topId){
           return success(flowTreeService.selectChildListById(flowTreeIds,level,flowModelId,topId));
    }


    /**
     * level=1 时flowModelId的实际值为主流程的id
     * @param flowModelId
     * @param level
     * @return
     */

    @GetMapping("/listByModelId")
    public Object list(@RequestParam("flowModeId") String flowModelId,@RequestParam("level") Integer level){

        List<FlowTree> list = flowTreeService.selectList(flowModelId,level);
        return success(list);
    }

    /**
     * 删除新版本里的节点(未发布的版本)
     * @return
     */
    @GetMapping("/deleteNode")
    public Object deleteNewVersionNode(@RequestParam("flowTreeId") String flowTreeId){
        Boolean bool= flowTreeService.delete(flowTreeId);
        if (bool){
            return success();
        }
    return error("删除失败！");
    }


    @PostMapping("/updateNewNode")
    public Object updateNewNode(@Validated @RequestBody UpdateNewNodeVO vo ){

        return success();
    }

    @GetMapping("/treeList")
    public Object treeList(@RequestParam("projectType") Integer projectType){
            return  success(flowTreeService.getTree(projectType));
    }

    @GetMapping("/listByType")
    public Object findListByType(@RequestParam("type") Integer type){
        return success(flowTreeService.listByType(type));
    }

        @GetMapping("/selectNameList")
    public Object mainNameListBySubName(@RequestParam("type") Integer type,@RequestParam("name") String name){
       return success(flowTreeService.nameList(type,name));
    }

    private void setSuperChildNum(List<FlowTree> treeList, FlowTree flowTree) {
        treeList.forEach(item->{
            if (null ==item.getHasChild() || Constants.TOP_PARENT.equals(item.getHasChild())){
                item.setHasChild(1);
            }else{
                item.setHasChild(item.getHasChild()+1);
            }
            item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
        });
    }

    /**
     * 封装路径
     * @param flowTree
     * @return
     */
    private String getPathIds(FlowTree flowTree,List<FlowTree> treeList){
        if (CollectionUtils.isNotEmpty(treeList)){
            ArrayList<String> pathList = Lists.newArrayList();
            treeList.forEach(item->{
                //多个路径之间用";"隔开
                String path = Splitter.on(";").trimResults().splitToList(item.getPathIds()).stream().map(item2 -> {
                    return item2.trim() + "," + flowTree.getId();
                }).collect(Collectors.joining(";"));
              pathList.add(path);
            });
            return Joiner.on(";").skipNulls().join(pathList);
        }
        return flowTree.getId();
    }







}
