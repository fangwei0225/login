package com.taoding.mp.base.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述
 *
 * @author Leon
 * @version 2018/11/8 16:58
 */
@Data
public class MenuVo implements Serializable {

    private Integer id;
    private Integer status;
    private String corpId;
    private String code;
    private String name;
    private String url;
    private String type;
    private Integer parentId;
    private Integer num;
    private String path;
    private String icon;
    private List<MenuVo> children = new ArrayList<>();
    private Integer pid = parentId;
    private String title = name;
    private Boolean open = false;

}
