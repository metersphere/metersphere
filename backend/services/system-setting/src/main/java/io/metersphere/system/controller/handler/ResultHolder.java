package io.metersphere.system.controller.handler;

import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import lombok.Data;

@Data
public class ResultHolder {
    // 请求是否成功
    private int code = MsHttpResultCode.SUCCESS.getCode();
    // 描述信息，一般是错误信息
    private String message;
    // 详细描述信息, 如有异常，这里是详细日志
    private Object messageDetail;
    // 返回数据
    private Object data = "";

    public ResultHolder() {
    }

    public ResultHolder(Object data) {
        this.data = data;
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

    public ResultHolder(int code, String msg, Object messageDetail, Object data) {
        this.code = code;
        this.message = msg;
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public static ResultHolder success(Object obj) {
        return new ResultHolder(obj);
    }

    public static ResultHolder error(int code, String message) {
        return new ResultHolder(code, message, null, null);
    }

    public static ResultHolder error(String message, String messageDetail) {
        return new ResultHolder(-1, message, messageDetail, null);
    }

    public static ResultHolder error(int code, String message, Object messageDetail) {
        return new ResultHolder(code, message, messageDetail, null);
    }

    /**
     * 用于特殊情况，比如接口可正常返回，http状态码200，但是需要页面提示错误信息的情况
     * @param code 自定义 code
     * @param message 给前端返回的 message
     * @return
     */
    public static ResultHolder successCodeErrorInfo(int code, String message) {
        return new ResultHolder(code, message, null, null);
    }
}
