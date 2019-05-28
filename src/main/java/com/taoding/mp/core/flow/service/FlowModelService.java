package com.taoding.mp.core.flow.service;

import com.taoding.mp.core.flow.entity.FlowModel;
import com.taoding.mp.core.flow.vo.FlowModelUpdateVO;
import com.taoding.mp.core.flow.vo.ModelListVO;

import java.util.List;

public interface FlowModelService {
    /**
     * 添加审批流程模板
     * @param flowModel
     * @return FlowModel
     */
   FlowModel add(FlowModel flowModel);

    /**
     * 修改审批流程模板
     * @param vo
     * @return
     */
   FlowModel update(FlowModelUpdateVO vo);

    /**
     * 删除模板
     * @param id
     * @return
     */
    Boolean delete(String id);

    /**
     * 列表（不带分页）
     * @return
     */
    List<FlowModel> findAll(ModelListVO vo);

    /**
     * 发行版本
     * @param modelId
     * @return
     */
    FlowModel released(String modelId);

    /**
     *  判断发布和未发布的版本是否存在
     * @param type
     * @return
     */
    Boolean versionExist(Integer type);
}
