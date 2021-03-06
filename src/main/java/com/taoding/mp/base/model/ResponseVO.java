package com.taoding.mp.base.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 接口请求返回VO
 *
 * @author wuwentan
 * @date 2018/8/10
 */
@Data
public class ResponseVO<T> {

    private Integer status;

    private String msg;

    /**
     * 扩展字段（注解：如果为空的话不返回该字段）
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object extra;

    private T data;

    public ResponseVO(T data) {
        this.status = 200;
        this.msg = "success";
        this.data = data;
    }

    public ResponseVO(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public ResponseVO(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public ResponseVO(Integer status, String msg, T data, Object extra) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.extra = extra;
    }

    public ResponseVO() {
    }
}
