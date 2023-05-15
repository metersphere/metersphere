package io.metersphere.sdk.controller.handler;

import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class ResultHolder {
    // 请求是否成功
    private int code = 0;
    // 描述信息
    private String message;
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

    public static ResultHolder success(Object obj) {
        return new ResultHolder(obj);
    }

    public static ResultHolder error(int code, String message) {
        return new ResultHolder(code, message, null);
    }

    public static ResultHolder error(int code, String message, Object object) {
        return new ResultHolder(code, message, object);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
