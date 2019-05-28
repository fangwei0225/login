package com.taoding.mp.core.work.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taoding.mp.util.BosUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description: 附件视图对象
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class AffixVO {
    /**
     * 附件名称
     */
    private String affixName;
    /**
     * 附件类型
     */
    private String affixType;
    /**
     * 附件大小--4541531byte
     */
    private String length;
    /**
     * 附件对象boskey
     */
    private String affixKey;

    public String getAffixUrl(){
        if(StringUtils.isNotBlank(this.affixKey)){
            return BosUtils.getUrlByBosKey(this.affixKey);
        }
        return null;
    }
}

