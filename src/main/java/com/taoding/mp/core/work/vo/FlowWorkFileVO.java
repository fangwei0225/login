package com.taoding.mp.core.work.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.core.flow.entity.FlowWorkFile;
import com.taoding.mp.core.work.entity.WorkRecord;
import com.taoding.mp.util.UpdateUtils;
import lombok.Data;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 用于封装 汇报材料 以及 附件的视图对象.
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class FlowWorkFileVO extends FlowWorkFile {
    private String flowWorkFileId;
    private WorkRecord workRecord;
    private String workLineId;
    private String flowModelId;
    private Integer stauts;

    public void conver(FlowWorkFile file){
        UpdateUtils.copyNonNullProperties(file, this);
        this.flowWorkFileId = this.getId();
    }
}
