package io.metersphere.controller.handler;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BaseResultHolder<T> {
    public BaseResultHolder() {
        this.success = true;
    }

    private BaseResultHolder(T data) {
        this.data = data;
        this.success = true;
    }

    private BaseResultHolder(boolean success, String msg) {
        this.success = success;
        this.message = msg;
    }

    private BaseResultHolder(boolean success, String msg, T data) {
        this.success = success;
        this.message = msg;
        this.data = data;
    }

    // 请求是否成功
    private boolean success = false;
    // 描述信息
    private String message;
    // 返回数据
    private T data;

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static BaseResultHolder success(Object obj) {
        return new BaseResultHolder(obj);
    }

    public static BaseResultHolder error(String message) {
        return new BaseResultHolder(false, message, null);
    }

    public static BaseResultHolder error(String message, Object object) {
        return new BaseResultHolder(false, message, object);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
