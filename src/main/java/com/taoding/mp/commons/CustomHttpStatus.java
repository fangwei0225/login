package com.taoding.mp.commons;

/**
 * 常用状态码自定义清单（可按需添加）
 * @author liuxinghong
 */

public enum CustomHttpStatus {

    OK(200, "success"),
    BAD_REQUEST(400, "请求出错"),
    UNAUTHORIZED(401, "没有登录"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "找不到页面"),
    NOT_USER(405, "用户不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器出错"),
    LOGIN_FAIL(1001,"登录异常！"),
    TOKEN_PARSER_FAIL(1002,"令牌解析异常！"),
    VALIDATED_FAIL(1004,"数据验证失败！"),

    DATA_NULL(501, "没有查询到数据"),
    AVE_FAIL(502, "保存数据失败"),
    DEL_FAIL(503, "删除数据失败"),

    CAPTCHA_NULL(601,"请输入验证码！"),
    CAPTCHA_ERROR(602,"验证码输入错误！"),
    CAPTCHA_INVALID(603,"验证码已失效！"),
    UPLOAD_FAIL(1003,"上传失败"),
    DELETE_FAIL(1004,"删除失败"),


    RELEASE_FAIL(1005,"版本发布失败，审核中项目版本切换失败！"),
    NOT_ALL_OK(1006, "请全部确认后再提交下一步."),
    CREATE_FAIL(1007,"创建失败，原版本没有流程节点");

    private final int value;

    private final String msg;

    CustomHttpStatus(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int value() {
        return value;
    }

    public String msg() {
        return msg;
    }
}
