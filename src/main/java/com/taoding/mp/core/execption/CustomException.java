package com.taoding.mp.core.execption;

import org.springframework.http.HttpStatus;

/**
 * 全局异常处理类
 *
 * @author youngsapling
 * @date 2019-04-08
 */
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private int status;
    private String msg;
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public CustomException() {
        super();
    }

    public CustomException(int status, String msg, HttpStatus httpStatus) {
        super(msg);
        this.status = status;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public CustomException(int status, String msg) {
        super(msg);
        this.status = status;
        this.msg = msg;
    }

    public int getstatus() {
        return status;
    }

    @Override
    public String getLocalizedMessage() {
        return msg;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
