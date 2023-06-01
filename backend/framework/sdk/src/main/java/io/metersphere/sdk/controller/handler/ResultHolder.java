package io.metersphere.sdk.controller.handler;

import lombok.Data;

@Data
public class ResultHolder {
    // 请求是否成功
    private int code = 0;
    // 描述信息，一般是错误信息
    private String message;
    // 详细描述信息, 如有异常，这里是详细日志
    private String messageDetail;
    // 返回数据
    private Object data = "";

    public ResultHolder() {
        this.code = 0;
    }

    public ResultHolder(Object data) {
        this.data = data;
        this.code = 0;
    }

    public ResultHolder(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public ResultHolder(int code, String msg, Object data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public ResultHolder(int code, String msg, String messageDetail, Object data) {
        this.code = code;
        this.message = msg;
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public static ResultHolder success(Object obj) {
        return new ResultHolder(obj);
    }

    public static ResultHolder error(int code, String message) {
        return new ResultHolder(code, message, null);
    }

    public static ResultHolder error(int code, String message, Object data) {
        return new ResultHolder(code, message, data);
    }

    public static ResultHolder error(String message, String messageDetail) {
        return new ResultHolder(-1, message, messageDetail, null);
    }

    public static ResultHolder error(int code, String message, String messageDetail) {
        return new ResultHolder(code, message, messageDetail, null);
    }
}
