package com.taoding.mp.commons;

import com.taoding.mp.base.model.ResponseVO;


/**
 * @author liuxinghong
 * @Description: controller统一response响应结果(业务controller直接调用静态方法.)
 * @date 2019/4/8 000814:46
 */
public class ResponseBuilder {

    /**
     * 设置成功响应
     */
    public static ResponseVO success() {
        return responseEntity(CustomHttpStatus.OK, null, CustomHttpStatus.OK.msg());
    }

    public static ResponseVO success(Object data) {
        return responseEntity(CustomHttpStatus.OK, data, CustomHttpStatus.OK.msg());
    }

    public static ResponseVO success(String msg) {
        return responseEntity(CustomHttpStatus.OK, null, msg);
    }

    /**
     * 设置失败响应
     */
    public static ResponseVO error(CustomHttpStatus code) {
        return responseEntity(code, null, code.msg());
    }

    public static ResponseVO error(CustomHttpStatus code, Object data) {
        return responseEntity(code, data, code.msg());
    }

    public static ResponseVO error() {
        return responseEntity(CustomHttpStatus.BAD_REQUEST, null, CustomHttpStatus.BAD_REQUEST.msg());
    }

    public static ResponseVO error(String msg) {
        return responseEntity(CustomHttpStatus.BAD_REQUEST, null, msg);
    }

    public static ResponseVO error(Object data, String msg) {
        return responseEntity(CustomHttpStatus.BAD_REQUEST, data, msg);
    }


    /**
     * 设置响应代码
     */
    private static ResponseVO responseEntity(CustomHttpStatus code, Object data, String msg) {
        ResponseVO vo = new ResponseVO();
        if (null != data) {
            vo.setData(data);
        }
        vo.setStatus(code.value());
        vo.setMsg(msg);
        return vo;
    }
}
