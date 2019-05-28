package com.taoding.mp.core.flow.service;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.flow.vo.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FlowTreeService {

   //项目办理流程调用接口 ======================================================
    /**
     * 根据当前id查询所有topid相同的数据，并根据pid分组
     * @param flowTreeId
     * @return
     */
    Map<String,List<FlowTree>> selectMapByPId(String flowTreeId);
    /**
     * （（科级）子流程级别）根据流程id查询下一次需要创建的子流程节点
     * @param flowTreeId
     * @return
     */
    List<FlowTree> selectByPId(String flowTreeId);
    /**
     * 查询项目创建成功后主流程流程入口
     * @param type
     * @param topId
     * @param pId
     * @return
     */
    FlowTree selectTopId(Integer type, String topId, String pId);
    /**
     * 项目创建时通过type查询第一个主流程节点及其自己点列表
     * @param type
     * @param topId
     * @param pId
     * @return
     */
    FlowTree selectSubListByType(Integer type, String topId, String pId);
    /**
     * 根据主流程节点id查询下一个主流程节点及其所有的子流程
     * @param flowTreeId
     * @return
     */
    List<FlowTree> selectNextMainNodeById(String flowTreeId);
    /**
     * 根据子流程末尾节点id查询下一个主流程节点入口节点对象（科级末尾流程转向下一个主流程入口节点）
     * @param flowTreeId
     * @return
     */
    List<FlowTree>  selectNextMainNode(String flowTreeId);

    /**
     * 根据主流程节点id获取当前主流程节点及其即将要创建的子流程
     * @param flowTreeId
     * @return
     */
   FlowTree  selectFlowTreeAndChildren(String flowTreeId);

    /**
     * 根据pId查询上一节点的集合
     * @param pId
     * @return
     */
    List<FlowTree> selectSuperList(String pId);

    /**
     * 根据主流程id查询对应的所有子流程列表
     * @param mainId
     * @return
     */
    List<FlowTree> listByMainId(String mainId);

    /**
     * 给企业端提供项目审批流程列表(返回所有主节点)
     * @param type
     * @return
     */
    List<FlowTree> orgMainListByType(Integer type);
    /**
     * 根据项目类型查询对应的所有流程列表(过滤省市区数据)
     * @param type 项目类型
     * @return
     */
    HashMap<String, FlowTree> listTreeByMainId(Integer type, Integer grade);


    // 版本切换相关接口=================================================
    /**
     * 新版本与旧版本对比(子流程如果有增加的节点将其topid的节点及 即将创建的子节点返回去（放到chilren里面）)
     * @return
     */
    List<FlowTree> getAddFlowTree(String flowModelId);

//web端相关接口========================================================================
    /**
     * 添加节点信息
     * @param flowTree
     * @param files
     * @return
     */
    FlowTree add(FlowTree flowTree, ArrayList<FlowWorkFile> files);

    /**
     * 更新列表数据
     * @param treeList
     */
    void updateSuperList(List<FlowTree> treeList);

    FlowTree selectById(String id);

    FlowTree save(FlowTree flowTree);

    /**
     * 分页分level查询
     * @param vo
     * @return
     */
    PageVO<FlowTree> pageList(FlowTreePageListVO vo);

    /**
     * 创建下一个版本
     * @param vo
     * @return
     */
    boolean updateVersion(VersionUpdateVO vo);

    /**
     * 预览图接口（web同调此接口）
     * @param flowModelId
     * @param level
     * @return
     */
    PreviewVO view(String flowModelId, Integer level);


    /**
     * 预览节点接口（app调此接口）
     * @param type
     * @param level
     * @return
     */
    PreviewVO viewByApp(String type, Integer level);

    /**
     * 根据主流程id返回其所有子流程节点并过滤掉与项目无关的菱形节点的数据(警告，同一个子流程最多只能出现一组菱形节点，超过会返回数据不准确)
     * @param grade 项目隶属关系：0无、1省级、2市级、3区级
     * @param topId 主流程节点id
     * @return
     */
    PreviewVO projectSubTree(String topId,Integer grade);



    /**
     * 添加新版本的节点信息
     * @param vo
     * @return
     */
    FlowTree addNewVersionNode(InsertNewNodeVO vo);

    /**
     * 根据id查询同level下当前节点下的子级列表
     * @param flowTreeId
     * @return
     */
    List<FlowTree> selectChildListById(String flowTreeId,Integer level,String flowModelId,String topId);

    /**
     * 删除节点
     * @param flowTreeId
     * @return
     */
    Boolean delete(String flowTreeId);

    List<FlowTree> selectList(String flowModelId,Integer level);

    /**
     * 详情
     * @param id
     * @return
     */
    FlowTreeUpdateVO detail(String id);

    /**
     * 根据项目类别获取最新的节点树
     * @param projectType
     * @return
     */
    List<TreeListVO> getTree(Integer projectType);

    List<TreeListVO> listByType(Integer type);

    /**
     * 根据子节点的名称查询对应主节点的名称列表
     * @param type
     * @param name
     * @return
     */
    List<String> nameList(Integer type,String name);

    /**
     * 根据id查询所有的子级（递归查询）（废弃）
     * @param flowTreeId
     * @return
     */
  // List<FlowTree> findAllChildrenById(String flowTreeId);
}
