package com.taoding.mp.core.flow.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.CustomHttpStatus;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.core.flow.dao.*;
import com.taoding.mp.core.flow.entity.*;
import com.taoding.mp.core.flow.service.FlowTreeService;
import com.taoding.mp.core.flow.vo.*;
import com.taoding.mp.util.CreateObjUtils;
import com.taoding.mp.util.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liuxinghong
 * @Description:流程树
 * @date 2019/4/16 001609:00
 */
@Service
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class FlowTreeServiceImpl extends BaseDAO implements FlowTreeService {

    @Autowired
   private JdbcTemplate jdbcTemplate;
    @Autowired
    private DeptRepository deptRepository;
    @Autowired
    private FlowTreeRepository flowTreeRepository;
    @Autowired
    private FlowWorkFileRepository flowWorkFileRepository;
    @Autowired
    private FlowModelRepository flowModelRepository;
    @Autowired
    private VersionIteratorRepository versionIteratorRepository;
    @Autowired
    private VersionUpdateFileRelRepository versionUpdateFileRelRepository;

    @Override
    public Map<String,List<FlowTree>> selectMapByPId(String flowTreeId) {
        Optional<FlowTree> flowTree = flowTreeRepository.findById(flowTreeId);
        List<FlowTree> flowTrees = flowTreeRepository.findByTopIdAndIsDelete(flowTree.get().getTopId(), Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(flowTrees)){
            HashMap<String,List<FlowTree>> result = Maps.newHashMap();
            flowTrees.forEach((item)->{
               Splitter.on(",").trimResults().splitToList(item.getParentId()).forEach(item2->{
                   if (result.containsKey(item2)){
                       result.get(item2).add(item);
                   }
                   else {
                       ArrayList<FlowTree> list = new ArrayList<>();
                       list.add(item);
                       result.put(item2,list);
                   }
               });
           });
            return result;
        }
        return null;
    }

    @Override
    public List<FlowTree> selectByPId(String flowTreeId) {
        Optional<FlowTree> byId = flowTreeRepository.findById(flowTreeId);
        String param ="%"+flowTreeId +"%";
        List<FlowTree> flowTreeList = flowTreeRepository.findByFlowModeIdAndParentIdLikeAndIsDelete(byId.get().getFlowModeId(), param, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(flowTreeList)){
            return flowTreeList.stream().filter(item->Constants.STATUE_NORMAL.equals(item.getIsDelete())).collect(Collectors.toList());
        }
            return null;
    }

    @Override
    public FlowTree selectTopId(Integer type, String topId, String pId) {
        //获取最新项目类型id
             String modelId= getModelIdByType(type);
             if (StringUtils.isNotBlank(modelId)) {
                 StringBuffer buffer = new StringBuffer().append("SELECT * FROM flow_tree f WHERE f.flow_mode_id = ")
                         .append(modelId)
                         .append(" AND f.is_delete = 1 AND f.top_id =")
                         .append(topId)
                         .append(" AND f.parent_id =")
                         .append(pId);
                 List<FlowTree> list = jdbcTemplate.query(buffer.toString(), new BeanPropertyRowMapper<>(FlowTree.class));
                 if (CollectionUtils.isNotEmpty(list)) {
                     FlowTree flowTree = list.get(0);
                     //查询该节点下科级节点第一级数据（可能多个）
                     List<FlowTree> treeList = flowTreeRepository.findFlowTreeByFlowModeIdAndTopIdAndParentIdAndIsDelete(modelId, flowTree.getId(), Constants.STRING_TOP_PARENT, Constants.STATUE_NORMAL);
                     if (CollectionUtils.isNotEmpty(treeList)) {
                         flowTree.setChildren(treeList);
                     }
                     return flowTree;
                 }
             }
        return null;
    }

    @Override
    public FlowTree selectSubListByType(Integer type, String topId, String pId) {
        //获取最新项目类型id
        String modelId= getModelIdByType(type);
        if (StringUtils.isNotBlank(modelId)) {
            StringBuffer buffer = new StringBuffer().append("SELECT * FROM flow_tree f WHERE f.flow_mode_id = ")
                    .append(modelId)
                    .append(" AND f.is_delete = 1 AND f.top_id =")
                    .append(topId)
                    .append(" AND f.parent_id =")
                    .append(pId);
            List<FlowTree> list = jdbcTemplate.query(buffer.toString(), new BeanPropertyRowMapper<>(FlowTree.class));
            if (CollectionUtils.isNotEmpty(list)) {
                FlowTree flowTree = list.get(0);
                //查询该节点下科级节点所有节点
                List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndTopIdAndLevelAndIsDelete(modelId,flowTree.getId(),Constants.FLOW_NODE_SUB_LEVEL,Constants.STATUE_NORMAL);
                if (CollectionUtils.isNotEmpty(treeList)) {
                    flowTree.setChildren(treeList);
                }
                return flowTree;
            }
        }
        return null;
    }

    @Override
    public List<FlowTree> selectNextMainNodeById(String flowTreeId) {
        Optional<FlowTree> tree = flowTreeRepository.findById(flowTreeId);
        String id="%"+tree.get().getId()+"%";
        List<FlowTree> trees = flowTreeRepository.findFlowTreeByParentIdLikeAndIsDelete(id, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(trees)){
            trees.forEach(item2->{
                item2.setChildren(flowTreeRepository.findByFlowModeIdAndTopIdAndLevelAndIsDelete(item2.getFlowModeId(),item2.getId(),Constants.FLOW_NODE_SUB_LEVEL,Constants.STATUE_NORMAL));
            });
        }
        return trees;
    }

    private String getModelIdByType(Integer type) {
      //  List<FlowModel> models = flowModelRepository.findByTypeAndIsEffectAndIsLatestAndIsDelete(type, Constants.VERSION_EFFECT_OK,Constants.VERSION_ISRELASR_TRUE, Constants.STATUE_NORMAL);
        String sql= "SELECT * FROM flow_model where is_effect =1 and is_latest =1 and is_delete =1 and type ="+type;
        List<FlowModel> flowModels = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FlowModel.class));
        if (CollectionUtils.isNotEmpty(flowModels)) {
            return flowModels.get(0).getId();
        }
        return null;
    }


    @Override
    public List<FlowTree> selectNextMainNode(String flowTreeId) {
        //获取子流程节点
        Optional<FlowTree> flowTree = flowTreeRepository.findById(flowTreeId);
        //获取当前主流程节点
        Optional<FlowTree> topTree = flowTreeRepository.findById(flowTree.get().getTopId());
        if (flowTree.isPresent()) {
            //查询下一个主流程(可能会并行)
            String param ="%"+topTree.get().getId() +"%";
            List<FlowTree> flowTreeList = flowTreeRepository.findFlowTreeByParentIdLikeAndIsDelete(param, Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(flowTreeList)) {
                flowTreeList.forEach(item2 -> {
                    //科级（子环节的首级节点）
                    List<FlowTree> nodeList = flowTreeRepository.findFlowTreeByTopIdAndParentIdAndIsDelete(item2.getId(), Constants.STRING_TOP_PARENT, Constants.STATUE_NORMAL);
                    item2.setChildren(nodeList);
                });
                return flowTreeList;
            }
        }
        return null;
    }

    @Override
    public FlowTree selectFlowTreeAndChildren(String flowTreeId) {
        Optional<FlowTree> flowTree = flowTreeRepository.findById(flowTreeId);
        String sql = "select * from flow_tree where top_id = ? and parent_id = 0 and is_delete = 1 ";
        List<Object> args = new ArrayList<>();
        args.add(flowTreeId);
        List<FlowTree> flowTrees = jdbc.query(sql, args.toArray(), new BeanPropertyRowMapper(FlowTree.class));
        if (CollectionUtils.isNotEmpty(flowTrees)){
            flowTree.get().setChildren(flowTrees.stream().filter(item->Constants.FLOW_NODE_SUB_LEVEL.equals(item.getLevel())).collect(Collectors.toList()));
        }
           return flowTree.get();
    }

    @Override
    public List<FlowTree> selectSuperList(String pId) {
        List<String> pathIds = Splitter.on(",").trimResults().splitToList(pId);
        return  flowTreeRepository.findAllById(pathIds);
    }

    @Override
    public List<FlowTree> listByMainId(String mainId) {
        List<FlowTree> flowTrees = flowTreeRepository.findByTopIdAndIsDelete(mainId, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(flowTrees)){
           return flowTrees.stream().filter(item2->Constants.FLOW_NODE_SUB_LEVEL.equals(item2.getLevel())).sorted(Comparator.comparing(FlowTree::getNum)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<FlowTree> orgMainListByType(Integer type) {
        String modelId = this.getModelIdByType(type);
        List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(modelId, Constants.FLOW_NODE_MAIN_LEVEL, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(treeList)){
           return treeList.stream().sorted(Comparator.comparing(FlowTree::getNum)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public HashMap<String, FlowTree> listTreeByMainId(Integer type,Integer grade) {
        String modelId = this.getModelIdByType(type);
        HashMap<String, FlowTree> result = Maps.newHashMap();
        List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(modelId, Constants.FLOW_NODE_MAIN_LEVEL, Constants.STATUE_NORMAL);
        List<FlowTree> subList = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(modelId, Constants.FLOW_NODE_SUB_LEVEL, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(treeList)){
            treeList.forEach(item->{
                if (CollectionUtils.isNotEmpty(subList)){
                    List<FlowTree> nodes = subList.stream().filter(item2 -> item.getId().equals(item2.getTopId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(nodes)){
                        //过滤数据
                        List<FlowTree> gradeList = nodes.stream().filter(item3 -> !Constants.PROJECT_GRADE_NONE.equals(item3.getGrade())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(gradeList)) {
                            //过滤数据
                            List<String> gradeIds = gradeList.stream().filter(item2 -> !grade.equals(item2.getGrade())).map(item2 -> item2.getId()).collect(Collectors.toList());
                            nodes.forEach(item4 -> {
                                gradeIds.forEach(item2 -> {
                                    if (!item4.getPathIds().contains(item2)) {
                                        result.put(item4.getId(),item4);
                                    }
                                });
                            });
                        }else {
                          nodes.forEach(item5->result.put(item5.getId(),item5));
                        }
                    }
                }
               result.put(item.getId(),item);
            });
            return result;
        }
        return null;
    }


    @Override
    public List<FlowTree> getAddFlowTree(String flowModelId) {
        List<FlowTree> newTreeList = flowTreeRepository.findByFlowModeIdAndIsDelete(flowModelId, Constants.STATUE_NORMAL);
        List<VersionIteratorRel> versionIteratorRelList = versionIteratorRepository.findAllByNewFlowModeIdAndIsEffectAndIsDelete(flowModelId, Constants.VERSION_EFFECT_OK, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(versionIteratorRelList)) {
            List<String> newIds = versionIteratorRelList.stream().map(item2 -> {
                return item2.getNewId();
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newTreeList)) {
                List<String> addNewTopIds = newTreeList.stream().map(item3 -> {
                    if (!newIds.contains(item3.getId())) {
                        return item3.getTopId();
                    }
                    return null;
                }).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(addNewTopIds)) {
                    List<FlowTree> parentList = newTreeList.stream().filter(item4 -> {
                        return addNewTopIds.contains(item4.getId());
                    }).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(parentList)){
                        parentList.forEach(item5->{
                            ArrayList<FlowTree> childrenList = Lists.newArrayList();
                            newTreeList.forEach(item6->{
                                if(item6.getTopId().equals(item5.getId())&&Constants.STRING_TOP_PARENT.equals(item6.getParentId())){
                                    childrenList.add(item6);
                                }
                            });
                           item5.setChildren(childrenList);
                        });
                    }
                    return parentList;
                }
            }
        }
        return null;
    }

    @Override
    public FlowTree add(FlowTree flowTree, ArrayList<FlowWorkFile> files) {
        if (CollectionUtils.isNotEmpty(files)){
               flowWorkFileRepository.saveAll(files);
        }
            return flowTreeRepository.save(flowTree);
    }

    @Override
    public void updateSuperList(List<FlowTree> treeList) {
        flowTreeRepository.saveAll(treeList);
    }

    @Override
    public FlowTree selectById(String id) {
        return flowTreeRepository.findById(id).get();

    }

    @Override
    public FlowTree save(FlowTree flowTree) {
        return flowTreeRepository.save(flowTree);

    }

    @Override
    public PageVO<FlowTree> pageList(FlowTreePageListVO vo) {
        int pageNo = vo.getPageNo() == null ? 1 : vo.getPageNo();
        int pageSize = vo.getPageSize() == null ? 15 : vo.getPageSize();
        String sql="";
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(vo.getLevel())){
            //主流程
             sql = "select * from flow_tree where is_delete = 1 and flow_mode_id="+vo.getFlowModeId()+" and level ="+vo.getLevel();
        }else {
            sql = "select * from flow_tree where is_delete = 1 and top_id="+vo.getFlowModeId()+" and level ="+vo.getLevel();
        }
        if (StringUtils.isNotBlank(vo.getName())){
            sql += " and name like "+"\'%"+vo.getName()+"%\'";
        }
        sql +=" order by num asc ,id asc";
       return getPage(sql,pageNo,pageSize,new ArrayList<>(),new BeanPropertyRowMapper(FlowTree.class));
    }

    @Override
    public boolean updateVersion(VersionUpdateVO vo) {
        /**
         * 1.创建模板类型，设置状态为未发布
         * 2.拷贝当前模板节点并更换为新的模板节点，入库，并将level=0级节点返回
         */
        Optional<FlowModel> model = flowModelRepository.findById(vo.getFlowModeId());
        FlowModel flowModel = CreateObjUtils.create(FlowModel.class);
        flowModel.setIsEffect(Constants.VERSION_EFFECT_NO);//未发布版本
        flowModel.setVersion(vo.getVersion());
        if (StringUtils.isNotBlank(vo.getName())){
            flowModel.setName(vo.getName());
        }else {
            flowModel.setName(model.get().getName());
        }
        flowModel.setType(model.get().getType());
        flowModel.setIsLatest(Constants.VERSION_ISRELASR_FALSE);
        //持久化到数据库
        List<FlowTree> list = flowTreeRepository.findByFlowModeIdAndIsDelete(vo.getFlowModeId(), Constants.STATUE_NORMAL);
        //进行节点的所有数据封装
        if (CollectionUtils.isNotEmpty(list)){
            List<FlowTree> nodeList = list.stream().map(item2->{
                FlowTree tree = new FlowTree();
                 BeanUtils.copyProperties(item2,tree);
                 return tree;
            }).collect(Collectors.toList());
            ArrayList<FlowWorkFile> fileList = Lists.newArrayList();
            LinkedList<VersionIteratorRel> iteratorList = Lists.newLinkedList();
            LinkedList<VersionUpdateFileRel> fileUpdateList = Lists.newLinkedList();
            List<String> flowTreeIds = nodeList.stream().map(FlowTree::getId).collect(Collectors.toList());
            List<FlowWorkFile> flowWorkFiles = flowWorkFileRepository.findAllByFlowTreeIdInAndIsDelete(flowTreeIds, Constants.STATUE_NORMAL);
            for (FlowTree tree:nodeList){
              String newId = IdWorker.createId();
              String oldId = tree.getId();
              String oldFlowModeId = tree.getFlowModeId();
              tree.setFlowModeId(flowModel.getId());
              tree.setId(newId);
              tree.setPathIds(StringUtils.replace(tree.getPathIds(),oldId,newId));
              tree.setCreateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
              tree.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
              //封装新旧版本id关系表
                VersionIteratorRel iteratorRel = CreateObjUtils.create(VersionIteratorRel.class);
                iteratorRel.setOldId(oldId);
                iteratorRel.setNewId(newId);
                iteratorRel.setOldFlowModeId(oldFlowModeId);
                iteratorRel.setNewFlowModeId(flowModel.getId());//新版本的id
                iteratorRel.setIsEffect(Constants.VERSION_EFFECT_NO);//设置为未生效，当启用新版本时才生效
                iteratorList.add(iteratorRel);
                //封装节点文件数据
                if (CollectionUtils.isNotEmpty(flowWorkFiles)){
                    flowWorkFiles.forEach(item->{
                        if (StringUtils.equals(item.getFlowTreeId(),oldId)){
                            String oldFileId = item.getId();
                            FlowWorkFile file = CreateObjUtils.create(FlowWorkFile.class);
                            file.setFlowTreeId(newId);
                            file.setName(item.getName());
                            file.setCreateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                            file.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                            fileList.add(file);
                        //维护版本迭代时申报资料的主键关系
                            VersionUpdateFileRel fileRel = CreateObjUtils.create(VersionUpdateFileRel.class);
                            fileRel.setOldFileId(oldFileId);
                            fileRel.setNewFileId(file.getId());
                            fileRel.setOldFlowModeId(oldFlowModeId);
                            fileRel.setNewFlowModeId(flowModel.getId());
                            fileRel.setIsEffect(Constants.VERSION_EFFECT_NO);
                            fileUpdateList.add(fileRel);
                        }
                    });
                }
              for (FlowTree tree2:nodeList){
                    if (StringUtils.equals(tree2.getTopId(),oldId)){
                       tree2.setTopId(newId);
                  }
                    if (tree2.getParentId().contains(oldId)){
                        tree2.setParentId(StringUtils.replace(tree2.getParentId(),oldId,newId));
                    }
                    if (idContons(tree2.getPathIds(),oldId)){
                            tree2.setPathIds(StringUtils.replace(tree2.getPathIds(),oldId,newId));
                    }
              }
          }
           //持久化到数据库
            flowWorkFileRepository.saveAll(fileList);
            flowModelRepository.save(flowModel);
            flowTreeRepository.saveAll(nodeList);
            versionIteratorRepository.saveAll(iteratorList);
            versionUpdateFileRelRepository.saveAll(fileUpdateList);
            return true;
        }else {
            throw new CustomException(CustomHttpStatus.CREATE_FAIL.value(),CustomHttpStatus.CREATE_FAIL.msg());
        }
    }

    /**
     * 判断是否包含该id
     * @param deptIds
     * @param oldId
     * @return
     */
    private boolean idContons(String deptIds, String oldId) {
        List<String> stringList = Splitter.on(";").trimResults().splitToList(deptIds);
        ArrayList<String> objects = Lists.newArrayList();
        stringList.forEach(item2->{
            Splitter.on(",").trimResults().splitToList(item2).forEach(item->objects.add(item));
        });
        return objects.contains(oldId);
    }

    @Override
    public PreviewVO view(String flowModelId, Integer level) {
        String sql="";
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)){
            //主流程
            sql = "select * from flow_tree where is_delete = 1 and flow_mode_id= ?  and level = ? order by num asc ,id asc";
        }else {
            sql = "select * from flow_tree where is_delete = 1 and top_id= ?  and level = ? order by num asc ,id asc";
        }
        List<ViewResponDataVO> treeList = jdbcTemplate.query(sql,new Object[]{flowModelId,level},new BeanPropertyRowMapper<>(ViewResponDataVO.class));
        List<Department> departmentList = deptRepository.findAll();
        if (CollectionUtils.isNotEmpty(treeList)){

            return getPreviewVO(flowModelId, level, treeList, departmentList);
        }
        return null;
    }

    private PreviewVO getPreviewVO(String flowModelId, Integer level, List<ViewResponDataVO> treeList, List<Department> departmentList) {
        ArrayList<DirectVO> directList = Lists.newArrayList();
        PreviewVO previewVO = new PreviewVO();
        //封装数据
        LinkedList<ViewDataVO> dataList = Lists.newLinkedList();
        //封装方向
        treeList.forEach(item->{
            Splitter.on(",").trimResults().splitToList(item.getParentId()).forEach(item2->{
                DirectVO directVO = new DirectVO();
                directVO.setFrom(item2);
                directVO.setTo(item.getId());
                directList.add(directVO);

            });
            if (Constants.TOP_PARENT.equals(item.getType())&& CollectionUtils.isNotEmpty(departmentList)){
                //封装部门科室
                    departmentList.forEach(item2->{
                        if (StringUtils.isNotBlank(item.getDeptIds())){
                            if (item.getDeptIds().equals(item2.getId())){
                                item.setDeptName((Optional.ofNullable(item.getDeptName()).orElse("")+" "+item2.getName()).trim());
                            }
                        }
                        if (StringUtils.isNotBlank(item.getUnitIds())){
                            if (item.getUnitIds().contains(item2.getId())){
                                item.setUnitName((Optional.ofNullable(item.getUnitName()).orElse("")+" "+item2.getName()).trim());
                            }
                        }
                    });
            }
            ViewDataVO dataVO = new ViewDataVO();
            dataVO.setKey(item.getId());
            dataVO.setText(item);
            dataList.add(dataVO);
        });
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)){
            //主流程模拟一条主入口节点
            ViewResponDataVO tree = new ViewResponDataVO();
            tree.setId("0");
            tree.setName("项目审批入口");
            tree.setIsDelete(Constants.STATUE_NORMAL);
            tree.setRemark("流程入口从此处开始");
            tree.setLevel(level);
           // tree.setFlowModeId(flowModelId);
            ViewDataVO dataVO = new ViewDataVO();
            dataVO.setKey(tree.getId());
            dataVO.setText(tree);
            dataList.addFirst(dataVO);
        }
        if (Constants.FLOW_NODE_SUB_LEVEL.equals(level)){
            //子流程模拟一条主入口节点
            Optional<FlowTree> top = flowTreeRepository.findById(treeList.get(0).getTopId());
            if (top.isPresent()){
                ViewResponDataVO tree = new ViewResponDataVO();
                BeanUtils.copyProperties(top.get(),tree);
                tree.setId("0");
                tree.setNum(0);
                tree.setUnitName(tree.getUnitName());
                ViewDataVO dataVO = new ViewDataVO();
                dataVO.setKey(tree.getId());
                dataVO.setText(tree);
                dataList.addFirst(dataVO);
            }
        }
        previewVO.setDataMap(dataList);
        previewVO.setDirectionList(directList);
        return previewVO;
    }

    @Override
    public PreviewVO viewByApp(String type, Integer level) {
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)){
           String id = getModelIdByType(Integer.valueOf(type));
           return this.view(id,level);
        }
       return this.view(type,level);
    }

    @Override
    public PreviewVO projectSubTree(String topId, Integer grade) {
        String sql = "select * from flow_tree where is_delete = 1 and top_id= ?  and level = ? order by num asc ,id asc";
        List<ViewResponDataVO> treeList = jdbcTemplate.query(sql,new Object[]{topId,Constants.FLOW_NODE_SUB_LEVEL},new BeanPropertyRowMapper<>(ViewResponDataVO.class));
        if (CollectionUtils.isNotEmpty(treeList)){
            List<Department> departmentList = deptRepository.findAll();
            //查询所有的菱形节点
        Integer[] grades ={1,2,3};
        List<FlowTree> gradeList = flowTreeRepository.findByTopIdAndGradeInAndLevelAndIsDelete(topId, grades, Constants.FLOW_NODE_SUB_LEVEL, Constants.STATUE_NORMAL);
            LinkedList<ViewResponDataVO> resultList = Lists.newLinkedList();
            if (CollectionUtils.isNotEmpty(gradeList)){
            //过滤数据
            List<String> gradeIds = gradeList.stream().filter(item -> !grade.equals(item.getGrade())).map(item2->item2.getId()).collect(Collectors.toList());
            treeList.forEach(item->{
                gradeIds.forEach(item2->{
                    if (!item.getPathIds().contains(item2)){
                        resultList.add(item);
                    }
                });
            });
                //封装预览图
              return   getPreviewVO(topId,Constants.FLOW_NODE_SUB_LEVEL,resultList,departmentList);
            }else {
              return getPreviewVO(topId,Constants.FLOW_NODE_SUB_LEVEL,treeList,departmentList);
            }
        }
        return null;
    }

    @Override
    public FlowTree addNewVersionNode(InsertNewNodeVO vo) {
        /**
         * 逻辑：
         * 1.判断level
         *      1): level=0 时
         *          a.查询原来链结构中父节点及子节点对象
         *          b.将新节点关联到中间（打开链接）(判断添加顶级节点和末级节点的情况)
         *              判断添加的节点的父对象原来所对应的子对象与添加的节点的childId的关系
         *          c.设置路径及其所有子节点的路径
         *      2): level=1 时
         *          a.
         */
        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(vo.getLevel())
                && Constants.STRING_TOP_PARENT.equals(vo.getTopId())
                && Constants.STRING_TOP_PARENT.equals(vo.getParentId())
                ) {
            //查询是否已经存在顶级节点
            List<FlowTree> topTree = flowTreeRepository.findFlowTreeByFlowModeIdAndTopIdAndParentIdAndIsDelete(vo.getFlowModeId(), Constants.STRING_TOP_PARENT, Constants.STRING_TOP_PARENT, Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(topTree) && Constants.FLOW_NODE_MAIN_LEVEL.equals(topTree.get(0).getLevel()) && StringUtils.isBlank(vo.getChildIds())) {
                String msg = "主流程的顶级节点:"+topTree.get(0).getName()+"已经存在";
                throw new CustomException(CustomHttpStatus.CREATE_FAIL.value(), msg);
            }
            //判断子节点是否为当前的顶级节点
            if (CollectionUtils.isNotEmpty(topTree) && Constants.FLOW_NODE_MAIN_LEVEL.equals(topTree.get(0).getLevel()) && StringUtils.isNotBlank(vo.getChildIds()) && !StringUtils.equals(vo.getChildIds(),topTree.get(0).getId())){
               String msg = "下级节点只能选择:"+topTree.get(0).getName();
                throw new CustomException(CustomHttpStatus.CREATE_FAIL.value(),msg);
            }
        }
        //获取所有当前环节的列表
        List<FlowTree> result = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(vo.getFlowModeId(), vo.getLevel(), Constants.STATUE_NORMAL);
        List<String> pIds = Splitter.on(",").trimResults().splitToList(vo.getParentId());
        List<FlowTree> parents = flowTreeRepository.findAllById(pIds);
        FlowTree flowTree = CreateObjUtils.create(FlowTree.class);
        BeanUtils.copyProperties(vo,flowTree);
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
            flowWorkFileRepository.saveAll(files);
        }else {
            flowTree.setHasFlowWorkFile(Constants.FLOW_FILE_NO);
        }
        //设置上级的子级个数
        //设置路径(设置当前节点的路径)
         flowTree.setPathIds(getPathIds(flowTree.getId(),parents));
        //不是叶子节点设置（
        //  1.如果添加的是顶级节点设置该节点以下的所有节点的topId
        //  2.设置子节点的pid和该节点已下的所有节点的路径
        // ）
        if (StringUtils.isNotBlank(vo.getChildIds())){
            //查询新加节点下所有的子级列表
            List<String> childs = Splitter.on(",").trimResults().splitToList(vo.getChildIds());
            List<FlowTree> allChildList= allChildTreeList(childs,result);//所有子级列表
            //判断插入的是level=0 的顶级节点
            if (Constants.FLOW_NODE_MAIN_LEVEL.equals(vo.getLevel())
                    && Constants.STRING_TOP_PARENT.equals(vo.getTopId())
                    && Constants.STRING_TOP_PARENT.equals(vo.getParentId())){
                //查询是否已经存在顶级节点
                //更改所有的topId
                if (CollectionUtils.isNotEmpty(allChildList)){
                    allChildList.forEach(item5->{
                        item5.setTopId(flowTree.getId());
                        item5.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                    });
                }
            }
            //该节点以下的所有节点的路径
            allChildList.forEach(child->{
                //设置当前节点的下一级的parentId及子级个数
                if (childs.contains(child.getId())){//设置当前节点的下一级的parentId及路径
                    Splitter.on(",").splitToList(vo.getParentId()).forEach(pid->{
                        if (child.getParentId().contains(pid)){
                            //替换parentId
                            child.setParentId(StringUtils.replace(child.getParentId(), pid, flowTree.getId()));
                        }else {
                            child.setParentId(child.getParentId()+","+flowTree.getId());
                        }
                    });
                }
                //设置所有下级的所有节点的路径路径
                    childs.forEach(item4 -> {
                        if (child.getPathIds().contains(item4)) {
                            child.setPathIds(StringUtils.replace(child.getPathIds(), item4, flowTree.getId() + "," + item4));
                        }
                    });
                child.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            });
            //设置子节点的个数
            flowTree.setHasChild(childs.size());
            flowTreeRepository.saveAll(allChildList);
        }
        FlowTree tree = flowTreeRepository.saveAndFlush(flowTree);
        //设置上级的子级个数
        parents.forEach(item->{
            String param ="%"+item.getId()+"%";
            List<FlowTree> treeList = flowTreeRepository.findFlowTreeByParentIdLikeAndIsDelete(param, Constants.STATUE_NORMAL);
            item.setHasChild(treeList.size());
            item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
        });
        flowTreeRepository.saveAll(parents);
        return tree;
    }




    //获取所有的子节点和叶子节点
    private List<FlowTree> allChildTreeList(List<String> childs,List<FlowTree> all) {
        HashSet<String> childrens = Sets.newHashSet();
        childs.forEach(item -> {
            List<FlowTree> flowTrees = flowTreeRepository.findByPathIdsLikeAndIsDelete("%" + item + "%", Constants.STATUE_NORMAL);
         //   List<FlowTree> flowTrees = this.findAllChildrenById(item);
            if (CollectionUtils.isNotEmpty(flowTrees)) {
                flowTrees.forEach(item2 -> childrens.add(item2.getId()));
            }
        });
        if (CollectionUtils.isNotEmpty(all)) {
            return all.stream().filter(item3 -> childrens.contains(item3.getId())).collect(Collectors.toList());
        }
        return null;
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

    private String getPathIds(String flowTreeId,List<FlowTree> parentList){
        if (CollectionUtils.isNotEmpty(parentList)) {
            HashSet<String> parents = Sets.newLinkedHashSet();
            parentList.forEach(item->{
                Splitter.on(",").trimResults().splitToList(item.getPathIds()).forEach(item2->{
                    parents.add(item2);
                });
            });
            return Joiner.on(",").skipNulls().join(parents)+","+flowTreeId;
        }
        return flowTreeId;
    }

    @Override
    public List<FlowTree> selectChildListById(String flowTreeId,Integer level,String flowModelId,String topId){
        List<String> ids = Splitter.on(",").trimResults().splitToList(flowTreeId);
        //返回所有节点
        LinkedList<FlowTree> list = Lists.newLinkedList();
        List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(flowModelId, level, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(treeList)){
            treeList.forEach(item->{
                if (!ids.contains(item.getId())){
                    list.add(item);
                }
            });
            List<FlowTree> collect = list.stream().sorted(Comparator.comparingInt(FlowTree::getNum)).collect(Collectors.toList());
            if (StringUtils.isNotBlank(topId)){
                return  collect.stream().filter(item->topId.equals(item.getTopId())).collect(Collectors.toList());
            }
            return collect;
        }

        /**返回下级节点
         HashSet<String> idList = Sets.newHashSet();
        ids.forEach(item2->{
           String param ="%"+item2+"%";
           List<FlowTree> flowTrees = flowTreeRepository.findByPathIdsLikeAndIsDelete(param, Constants.STATUE_NORMAL);
           if (CollectionUtils.isNotEmpty(flowTrees)){
               flowTrees.stream().forEach(item6->{
                   idList.add(item6.getId());
               });
           }
            idList.remove(item2);
        });

        List<FlowTree> resultList = flowTreeRepository.findAllById(idList);
        if (CollectionUtils.isNotEmpty(resultList)){
            List<FlowTree> collect = resultList.stream().filter(item -> level.equals(item.getLevel())).filter(item2 -> flowModelId.equals(item2.getFlowModeId())).sorted(Comparator.comparingInt(FlowTree::getNum)).collect(Collectors.toList());
            if (StringUtils.isNotBlank(topId)){
              return  collect.stream().filter(item->topId.equals(item.getTopId())).collect(Collectors.toList());
            }
           return collect;
        }
     **/
        return null;
    }

    @Override
    public Boolean delete(String flowTreeId) {
        Optional<FlowTree> flowTree = flowTreeRepository.findById(flowTreeId);
        if (flowTree.isPresent()){
            FlowTree tree = flowTree.get();
            //父节点列表
            List<String> pIds = Splitter.on(",").trimResults().splitToList(tree.getParentId());
            List<FlowTree> parentList = flowTreeRepository.findAllById(pIds);
            //直接子节点列表
            String param ="%"+tree.getId()+"%";
            List<FlowTree> childrenList = flowTreeRepository.findFlowTreeByParentIdLikeAndIsDelete(param, Constants.STATUE_NORMAL);
            //所有的子节点包括叶子节点列表
            List<FlowTree> allChildrenNodes = flowTreeRepository.findByPathIdsLikeAndIsDelete(param, Constants.STATUE_NORMAL);
                allChildrenNodes.remove(tree);
            //主流程节点（单独处理顶级节点）
            if (Constants.FLOW_NODE_MAIN_LEVEL.equals(tree.getLevel())){
                if (Constants.STRING_TOP_PARENT.equals(tree.getTopId())&&Constants.STRING_TOP_PARENT.equals(tree.getParentId())){
                   throw new CustomException(CustomHttpStatus.DELETE_FAIL.value(),"无法直接删除顶级节点，请先创建一个顶级节点再删除");
                }
                //删除主流程下对应的子流程
                List<FlowTree> childList = flowTreeRepository.findByTopIdAndIsDelete(tree.getId(), Constants.STATUE_NORMAL);
                if (CollectionUtils.isNotEmpty(childList)){
                    childList.forEach(item->{
                        item.setIsDelete(Constants.STATUE_DEL);
                        item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                    });
                   flowTreeRepository.saveAll(childList);
                   //删除子流程对应的申报材料
                    List<FlowWorkFile> files = flowWorkFileRepository.findAllByFlowTreeIdInAndIsDelete(childList.stream().map(item -> item.getId()).collect(Collectors.toList()), Constants.STATUE_NORMAL);
                    if (CollectionUtils.isNotEmpty(files)){
                        files.forEach(item2->{
                            item2.setIsDelete(Constants.STATUE_DEL);
                            item2.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                        });
                        flowWorkFileRepository.saveAll(files);
                    }
                }
            }
             //6.子流程的第一个节点时单独处理
            if(Constants.FLOW_NODE_SUB_LEVEL.equals(tree.getLevel())
                    &&Constants.STRING_TOP_PARENT.equals(tree.getParentId())){
                //删除子流程第一个节点
                if (CollectionUtils.isNotEmpty(childrenList)){
                    childrenList.forEach(item->{
                        item.setParentId(Constants.STRING_TOP_PARENT);//设置为0
                        item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                    });
                    flowTreeRepository.saveAll(childrenList);
                }
            }else {
                /**
                 * 1.没有子节点时，直接删除
                 * 2.有多个兄弟节点，并且其子节点的parentId包含兄弟节点的id：直接删除
                 * 3.有多个兄弟节点，并且其子节点的parentId不包含兄弟节点的id 将其父节点及其子节点相连
                 * 4.没有兄弟节点的，将其上下相连（父节点或者子节点可能有多个）
                 * 5.当有多个父节点和多个子节点时 ，不能删除
                 */
                if (CollectionUtils.isNotEmpty(childrenList)&&childrenList.size()>1&&pIds.size()>1){
                    throw new CustomException(CustomHttpStatus.DELETE_FAIL.value(),"该节点同时有多个父节点和子节点无法删除！");
                }
                if (CollectionUtils.isNotEmpty(childrenList)){
                    for(FlowTree item:childrenList){
                        List<String> list = Splitter.on(",").trimResults().splitToList(item.getParentId());
                        LinkedList<String> ids = Lists.newLinkedList();
                        ids.addAll(list);
                        ids.remove(tree.getId());
                         if (CollectionUtils.isNotEmpty(ids)){
                             List<FlowTree> brotherList = flowTreeRepository.findAllById(ids);
                             //判断兄弟节点是否与父节点有关联 true---有关联   fale---无关联
                               Boolean bool = isRelation(brotherList,pIds);
                               if (bool){
                                   ArrayList<String> parentIds = Lists.newArrayList();
                                   parentIds.addAll(Splitter.on(",").trimResults().splitToList(item.getParentId()));
                                   parentIds.remove(tree.getId());
                                   item.setParentId(Joiner.on(",").skipNulls().join(parentIds));
                                   break ;
                               }
                         }
                        //没有兄弟节点，将其父级和子级相连(建立关系),注意其子级的父级id 有可能为不同级别的id
                        String replace = StringUtils.replace(item.getParentId(), tree.getId(), tree.getParentId());
                        item.setParentId(replace);
                         item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                    }
                    //设置父节点的子级个数
                    //todo:子级个数设置为如此
                    setChildreNodeNum(tree,parentList,childrenList);
                    flowTreeRepository.saveAll(parentList);
                    flowTreeRepository.saveAll(childrenList);
                }
            }
            //设置路径信息
            if (CollectionUtils.isNotEmpty(allChildrenNodes)){
                allChildrenNodes.forEach(item->{
                    item.setPathIds(StringUtils.replace(item.getPathIds(),","+tree.getId()+",",","));
                    item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                });
                flowTreeRepository.saveAll(allChildrenNodes);
            }
            //数据删除操作（逻辑删除）
            List<FlowWorkFile> files = flowWorkFileRepository.findAllByFlowTreeIdAndIsDelete(tree.getId(), Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(files)){
                List<String> ids = files.stream().map(item3 -> item3.getId()).collect(Collectors.toList());
                List<VersionUpdateFileRel> versionUpdateFileRels = versionUpdateFileRelRepository.findByNewFileIdInAndIsDelete(ids, Constants.STATUE_NORMAL);
               if (CollectionUtils.isNotEmpty(versionUpdateFileRels)){
                   versionUpdateFileRels.forEach(item4->{
                       item4.setIsDelete(Constants.STATUE_DEL);
                       item4.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                   });
                   versionUpdateFileRelRepository.saveAll(versionUpdateFileRels);
               }
                files.forEach(item2->{
                    item2.setIsDelete(Constants.STATUE_DEL);
                    item2.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                });
                flowWorkFileRepository.saveAll(files);
            }
            List<VersionIteratorRel> versionIteratorRels = versionIteratorRepository.findByNewIdAndIsDelete(tree.getId(), Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(versionIteratorRels)){
                VersionIteratorRel iteratorRel = versionIteratorRels.get(0);
                iteratorRel.setIsDelete(Constants.STATUE_DEL);
                iteratorRel.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                versionIteratorRepository.save(iteratorRel);
            }
            tree.setIsDelete(Constants.STATUE_DEL);
            tree.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
          flowTreeRepository.save(tree);
          return true;
        }
        return false;
    }

    /**
     * 设置父节点的子级个数
     * @param tree
     * @param parentList
     * @param childrenList
     */
    private void setChildreNodeNum(FlowTree tree, List<FlowTree> parentList, List<FlowTree> childrenList) {
        /**
         * 子级 多个 父级 多个  不存在
         * 1.父级 1个  子级 1个
         * 2.父级多个   子级 1个
         * 3.父级1个  子级 多个
         */
        if (parentList.size() == 1 && childrenList.size() ==1){
            return;
        }
        if (parentList.size()>1 && childrenList.size()==1){
            return;
        }
        if (parentList.size()==1 && childrenList.size()>1){
            parentList.get(0).setHasChild(childrenList.size());
            parentList.get(0).setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
        }

    }

    @Override
    public List<FlowTree> selectList(String flowModelId,Integer level) {
        List<FlowTree> list=Lists.newArrayList();

        if (Constants.FLOW_NODE_MAIN_LEVEL.equals(level)){//主流程
            FlowTree tree = new FlowTree();
            tree.setId("0");
            tree.setParentId("0");
            tree.setTopId("0");
            tree.setName("根节点");
            list.add(tree);
            List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(flowModelId, level,Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(treeList)){
                list.addAll(treeList.stream().sorted(Comparator.comparingInt(FlowTree::getNum)).collect(Collectors.toList()));
            }
        }else {//子流程
            Optional<FlowTree> byId = flowTreeRepository.findById(flowModelId);
            FlowTree tree = new FlowTree();
            tree.setId("0");
            tree.setParentId("0");
            tree.setTopId("0");
            tree.setName(byId.get().getName());
            list.add(tree);
            List<FlowTree> treeList = flowTreeRepository.findByFlowModeIdAndTopIdAndIsDelete(byId.get().getFlowModeId(),flowModelId, Constants.STATUE_NORMAL);
            if (CollectionUtils.isNotEmpty(treeList)){
                treeList.stream().sorted(Comparator.comparingInt(FlowTree::getNum)).forEach(item->{
                    if (level.equals(item.getLevel())){
                        list.add(item);
                    }
                });
            }
        }
        return list;
    }

    @Override
    public FlowTreeUpdateVO detail(String id) {
        Optional<FlowTree> flowTree = flowTreeRepository.findById(id);
        FlowTreeUpdateVO vo = new FlowTreeUpdateVO();
        BeanUtils.copyProperties(flowTree.get(),vo);
        if (StringUtils.isNotBlank(flowTree.get().getUnitIds())){
            List<String> unitList = Splitter.on(",").trimResults().splitToList(flowTree.get().getUnitIds());
            List<String> collect = deptRepository.findAllById(unitList).stream().map(item -> {
                return item.getName();
            }).collect(Collectors.toList());
            vo.setUnitName(Joiner.on(",").skipNulls().join(collect));
        }
        if (StringUtils.isNotBlank(flowTree.get().getDeptIds())){
            List<String> deptList = Splitter.on(",").trimResults().splitToList(flowTree.get().getDeptIds());
            List<String> collect2 = deptRepository.findAllById(deptList).stream().map(item -> {
                return item.getName();
            }).collect(Collectors.toList());
            vo.setDeptIdName(Joiner.on(",").skipNulls().join(collect2));
        }
        List<FlowWorkFile> files = flowWorkFileRepository.findAllByFlowTreeIdAndIsDelete(id, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(files)){
            vo.setFileName(files.stream().map(item -> {
                return item.getName();
            }).collect(Collectors.joining(",")));
        }
        return vo;
    }

    @Override
    public List<TreeListVO> getTree(Integer projectType) {
        String modelId = this.getModelIdByType(projectType);
        String sql= "SELECT id,`name`,`level`,top_id,num FROM flow_tree WHERE flow_mode_id = ? AND `level`=? AND is_delete =1 ORDER BY num ASC ";
        List<TreeListVO> mainList =jdbcTemplate.query(sql,new Object[]{modelId,Constants.FLOW_NODE_MAIN_LEVEL},new BeanPropertyRowMapper<>(TreeListVO.class));
        List<TreeListVO> subList = jdbcTemplate.query(sql,new Object[]{modelId,Constants.FLOW_NODE_SUB_LEVEL},new BeanPropertyRowMapper<>(TreeListVO.class));
        if (CollectionUtils.isNotEmpty(mainList)&& CollectionUtils.isNotEmpty(subList)){
            Map<String, List<TreeListVO>> subMap = subList.stream().collect(Collectors.groupingBy(TreeListVO::getTopId));
            mainList.forEach(item->{
                item.setChildren(subMap.get(item.getId()).stream().sorted(Comparator.comparingInt(TreeListVO::getNum)).collect(Collectors.toList()));
            });
            return mainList;
        }
        return null;
    }

    @Override
    public List<TreeListVO> listByType(Integer type) {
        String modelId = this.getModelIdByType(type);
        String sql="SELECT t.id ,t.NAME ,t.LEVEL,t.top_id  FROM flow_tree t WHERE t.flow_mode_id =?  and t.is_delete=1  ORDER BY t.LEVEL ASC ,t.num ASC;";
        return jdbcTemplate.query(sql,new Object[]{modelId},new BeanPropertyRowMapper<>(TreeListVO.class));
    }

    @Override
    public List<String> nameList(Integer type,String name) {
        String modelId= this.getModelIdByType(type);
        name = "%"+name+"%";
        List<FlowTree> list = flowTreeRepository.findByFlowModeIdAndNameLikeAndIsDeleteAndLevel(modelId, name, Constants.STATUE_NORMAL, Constants.FLOW_NODE_SUB_LEVEL);
        if (CollectionUtils.isNotEmpty(list)){
            Set<String> topIds = list.stream().collect(Collectors.groupingBy(FlowTree::getTopId)).keySet();
           return   flowTreeRepository.findAllById(topIds).stream().map(item->{
                 return item.getName();
             }).collect(Collectors.toList());
        }
        return null;
    }

    /**
    @Override
    public List<FlowTree> findAllChildrenById(String flowTreeId) {
        HashSet<String> resultList = Sets.newHashSet();
        Optional<FlowTree> tree = flowTreeRepository.findById(flowTreeId);
        List<FlowTree> data = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(tree.get().getFlowModeId(), tree.get().getLevel(), Constants.STATUE_NORMAL);
        resultList.add(tree.get().getId());
        ArrayList<FlowTree> trees = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(data)){
            getChildren(flowTreeId,data,resultList);
        }
        if (CollectionUtils.isNotEmpty(data)){
              data.forEach(item->{
                if (resultList.contains(item.getId())){
                    trees.add(item);
                }
            });
        }
        return trees;
    }

    private void getChildren(String param, List<FlowTree> data,HashSet<String> resultList) {
               for (FlowTree item : data) {
                   if (item.getParentId().contains(param)) {
                       resultList.add(item.getId());
                       param=item.getId();
                       this.getChildren(param,data,resultList);
                   }
       }
    }
**/

    /**
     * 判断兄节点与上级父节点的关系
     * @param brotherList
     * @param pIds
     * @return
     */
    Boolean isRelation( List<FlowTree> brotherList,List<String> pIds){
        for(FlowTree item:brotherList){
            List<String> toList = Splitter.on(",").trimResults().splitToList(item.getParentId());
            for(String item2:toList){
                if (pIds.contains(item2)){
                   return true;
                }
            }
        }
        return false;
    }

}
