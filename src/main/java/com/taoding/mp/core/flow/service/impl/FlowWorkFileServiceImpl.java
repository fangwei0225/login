package com.taoding.mp.core.flow.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.flow.dao.FlowTreeRepository;
import com.taoding.mp.core.flow.dao.FlowWorkFileRepository;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.flow.service.FlowWorkFileService;
import com.taoding.mp.core.flow.vo.FileUpdateVO;
import com.taoding.mp.util.CreateObjUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/16 001617:45
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class FlowWorkFileServiceImpl extends BaseDAO implements FlowWorkFileService {

    @Autowired
    private FlowWorkFileRepository flowWorkFileRepository;
    @Autowired
    private FlowTreeRepository flowTreeRepository;

    @Override
    public List<FlowWorkFile> selectByFlowTreeId(String flowTreeId,Integer isDel) {
           return flowWorkFileRepository.findAllByFlowTreeIdAndIsDelete(flowTreeId,isDel);
    }

    @Override
    public Boolean update(FileUpdateVO vo) {
        LinkedList<FlowWorkFile> list = Lists.newLinkedList();
        List<FlowWorkFile> files = flowWorkFileRepository.findAllByFlowTreeIdAndIsDelete(vo.getFlowTreeId(), Constants.STATUE_NORMAL);
        List<String> names = Splitter.on(",").trimResults().splitToList(vo.getFileNames());
        names.forEach(item->{
            if (StringUtils.isNotBlank(item)) {
                FlowWorkFile file = CreateObjUtils.create(FlowWorkFile.class);
                file.setFlowTreeId(vo.getFlowTreeId());
                file.setName(item);
                list.add(file);
            }
        });
        files.forEach(item2->{
            item2.setIsDelete(Constants.STATUE_DEL);
            item2.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
            list.add(item2);
        });
        flowWorkFileRepository.saveAll(list);
        return true;
    }

    @Override
    public List<FlowWorkFile> fileListByTopId(String id) {
        List<FlowTree> list = flowTreeRepository.findByTopIdAndIsDelete(id, Constants.STATUE_NORMAL);
        if (CollectionUtils.isNotEmpty(list)){
            List<String> ids = list.stream().filter(item -> Constants.FLOW_NODE_SUB_LEVEL.equals(item.getLevel())).map(item2 -> item2.getId()).collect(Collectors.toList());
           return flowWorkFileRepository.findAllByFlowTreeIdInAndIsDelete(ids,Constants.STATUE_NORMAL);
        }
        return null;
    }
}
