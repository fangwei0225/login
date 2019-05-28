package com.taoding.mp.core.flow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/4/22 002214:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateVO implements Serializable {
    private String flowTreeId;
    private String fileNames;
}
