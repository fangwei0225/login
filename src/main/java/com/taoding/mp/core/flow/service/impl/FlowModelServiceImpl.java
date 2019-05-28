package com.taoding.mp.core.flow.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.DataDicRepository;
import com.taoding.mp.base.entity.DataDictionary;
import com.taoding.mp.base.service.DataDicService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.commons.CustomHttpStatus;
import com.taoding.mp.core.execption.CustomException;
import com.taoding.mp.core.flow.dao.FlowModelRepository;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.VersionIteratorRepository;
import com.taoding.mp.core.flow.dao.VersionUpdateFileRelRepository;
import com.taoding.mp.core.flow.entity.FlowModel;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.VersionIteratorRel;
import com.taoding.mp.core.flow.entity.VersionUpdateFileRel;
import com.taoding.mp.core.flow.service.FlowModelService;
import com.taoding.mp.core.flow.vo.FlowModelUpdateVO;
import com.taoding.mp.core.flow.vo.ModelListVO;
import com.taoding.mp.core.work.service.EvolutionService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/15 001515:38
 */
@Service
@Transactional(rollbackFor = {Exception.class})
@Slf4j
public class FlowModelServiceImpl extends BaseDAO implements FlowModelService {

    @Autowired
    private FlowModelRepository flowModelRepository;
    @Autowired
    private FlowTreeRepository flowTreeRepository;
    @Autowired
    private EvolutionService evolutionService;
    @Autowired
    private DataDicRepository dataDicRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private VersionIteratorRepository versionIteratorRepository;
    @Autowired
    private VersionUpdateFileRelRepository versionUpdateFileRelRepository;

    @Override
    public FlowModel add(FlowModel flowModel) {
        return flowModelRepository.save(flowModel);
    }


    @Override
    public FlowModel update(FlowModelUpdateVO vo) {
        Optional<FlowModel> model = flowModelRepository.findById(vo.getId());
        if (model.isPresent()) {
            model.get().setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            BeanUtils.copyProperties(vo, model.get());
            return flowModelRepository.save(model.get());
        }
        return null;
    }

    @Override
    public Boolean delete(String id) {
        Optional<FlowModel> byId = flowModelRepository.findById(id);
        if (Constants.VERSION_ISRELASR_TRUE.equals(byId.get().getIsLatest())){
            throw new CustomException(CustomHttpStatus.DELETE_FAIL.value(),"正在使用的版本不能删除");
        }
        if (byId.isPresent()){
            byId.get().setIsDelete(Constants.STATUE_DEL);
            byId.get().setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            FlowModel save = flowModelRepository.save(byId.get());
            if (null !=save){
                List<FlowTree> flowTrees = flowTreeRepository.findByFlowModeIdAndIsDelete(id, Constants.STATUE_NORMAL);
                if (CollectionUtils.isNotEmpty(flowTrees)){
                    flowTrees.forEach(item->{
                        item.setIsDelete(Constants.STATUE_DEL);
                        item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                    });
                    List<FlowTree> treeList = flowTreeRepository.saveAll(flowTrees);
                    if (CollectionUtils.isNotEmpty(treeList)){
                        return true;
                    }
                    return false;
                }
            }
        }
    return true;
    }

    @Override
    public List<FlowModel> findAll(ModelListVO vo) {
        // List<FlowModel> modelsq = flowModelRepository.findAll(Sort.by("createTime").descending());
        StringBuffer buffer = new StringBuffer();
        List<Object> param = new ArrayList<>();
        buffer.append("select * from flow_model where is_delete =1  ");
        if (StringUtils.isNotBlank(vo.getName())) {
            buffer.append(" and name LIKE ?");
            param.add("%" + vo.getName() + "%");
        }
        if (null != vo.getType()) {
            buffer.append(" and type = ? ");
            param.add(vo.getType());
        }
        buffer.append("order by id desc");
        List<FlowModel> models = jdbc.query(buffer.toString(), param.toArray(), new BeanPropertyRowMapper<>(FlowModel.class));
        if (CollectionUtils.isNotEmpty(models)) {
            //设置当前最新版本的create为false；
            models.forEach(item4 -> {
                if (Constants.VERSION_EFFECT_OK.equals(item4.getIsEffect())
                        && Constants.STATUE_NORMAL.equals(item4.getIsDelete())
                        && Constants.VERSION_ISRELASR_TRUE.equals(item4.getIsLatest())) {
                    item4.setIsCreateNewVersion(true);
                    models.stream().collect(Collectors.groupingBy(FlowModel::getType)).forEach((key, value) -> {
                        value.forEach(item2 -> {
                            if (item4.getType().equals(item2.getType())
                                    && Constants.VERSION_EFFECT_NO.equals(item2.getIsEffect())
                                    && Constants.STATUE_NORMAL.equals(item2.getIsDelete())
                                    && Constants.VERSION_ISRELASR_FALSE.equals(item2.getIsLatest())) {
                                item4.setIsCreateNewVersion(false);
                                return;
                            }
                        });
                    });
                }
                //封装typeName
                DataDictionary dataValueAndIsDelete = dataDicRepository.findByTypeAndDataValueAndIsDelete(Constants.DATA_TYPE, item4.getType(), Constants.STATUE_NORMAL);
                item4.setTypeName(dataValueAndIsDelete.getName());
            });
        }
        return models;
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
    public FlowModel released(String modelId) {
        Optional<FlowModel> flowModel = flowModelRepository.findById(modelId);
        flowModel.get().setIsEffect(Constants.VERSION_EFFECT_OK);
        flowModel.get().setIsLatest(Constants.VERSION_ISRELASR_TRUE);
        flowModel.get().setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
        //判断该版本下是否有流程节点
        List<FlowTree> flowTreeMain = flowTreeRepository.findByFlowModeIdAndLevelAndIsDelete(modelId, Constants.FLOW_NODE_MAIN_LEVEL, Constants.STATUE_NORMAL);
        if (CollectionUtils.isEmpty(flowTreeMain)){
            throw new CustomException(CustomHttpStatus.RELEASE_FAIL.value(),"流程节点为空，发布失败");
        }
        //判断是否为第一次发布
        String byType = getModelIdByType(flowModel.get().getType());
        if (StringUtils.isNotBlank(byType)){
        //更改新旧节点关系表状态
        List<VersionIteratorRel> iteratorRels = versionIteratorRepository.findAllByNewFlowModeIdAndIsEffectAndIsDelete(modelId, Constants.VERSION_EFFECT_NO, Constants.STATUE_NORMAL);
        List<VersionUpdateFileRel> fileRels = versionUpdateFileRelRepository.findByNewFlowModeIdAndIsEffectAndIsDelete(modelId, Constants.VERSION_EFFECT_NO, Constants.STATUE_NORMAL);
        List<FlowModel> versionList = flowModelRepository.findByTypeAndIsEffectAndIsLatestAndIsDelete(flowModel.get().getType(), Constants.VERSION_EFFECT_OK, Constants.VERSION_ISRELASR_TRUE, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(versionList)){
            FlowModel model = versionList.get(0);
            //设置原来的版本的setIsLatest为FALSE
            model.setIsLatest(Constants.VERSION_ISRELASR_FALSE);
            model.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            flowModelRepository.save(model);
        }
        if (CollectionUtils.isNotEmpty(iteratorRels)){
            iteratorRels.forEach(item->{
                item.setIsEffect(Constants.VERSION_EFFECT_OK);
                item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            });
            versionIteratorRepository.saveAll(iteratorRels);
        }
        if (CollectionUtils.isNotEmpty(fileRels)){
            fileRels.forEach(item2->{
                item2.setIsEffect(Constants.VERSION_EFFECT_OK);
                item2.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            });
            versionUpdateFileRelRepository.saveAll(fileRels);
        }
        boolean evolution = evolutionService.evolution(modelId);
        if (!evolution){
            throw new CustomException(CustomHttpStatus.RELEASE_FAIL.value(),CustomHttpStatus.RELEASE_FAIL.msg());
        }
        }
        FlowModel save = flowModelRepository.save(flowModel.get());
        return save;
    }

    @Override
    public Boolean versionExist(Integer type) {
        List<FlowModel> flowModels = flowModelRepository.findByTypeAndIsDelete(type, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(flowModels)){
            return false;
        }
            return true;
    }


}
