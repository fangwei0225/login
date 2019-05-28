package com.taoding.mp.core.work.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.base.model.BaseEntity;
import com.taoding.mp.core.work.vo.AffixVO;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * 申报材料历史库
 * @author youngsapling
 * @date 2019/4/14
 */
@Data
@Entity
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@Table(name = "work_record_history")
public class WorkRecordHistory extends BaseEntity {

    /**
     * 项目审批流水线Id,关联WorkLine主键id
     */
    @Column
    private String workLineId;

    /**
     * 审批流程节点id,关联FlowTree主键id
     */
    @Column
    private String flowTreeId;

    /**
     * 申报材料节点id, 关联FlowWorkFile主键id
     */
    @Column
    private String flowWorkFileId;

    /**
     * 所属模板主键, 在更新的时候使用旧id直接插到所有要更新的.
     */
    @Column
    private String flowModelId;

    /**
     * type 0:备注     1:申报材料
     */
    @Column
    private Integer type;

    /**
     * 备注
     */
    @Lob
    @Column(columnDefinition="text")
    private String remark;

    /**
     * 申报材料附件
     */
    @Lob
    @Column(columnDefinition="text")
    private String affix;

    @Transient
    private List<AffixVO> affixList;

    /**
     *baseWork.setAffix(JSONArray.toJSONString(baseWork.getAffixVOList(), new SimplePropertyPreFilter(AffixVO.class, "affixName", "affixKey", "length")));
     */

    /**
     * 申报材料确认状态：0待确认，1已确认
     */
    @Column
    private Integer status;
}
