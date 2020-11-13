package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONType(typeName = RequestType.TCP)
public class TCPRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.TCP;
    @JSONField(ordinal = 51)
    private String classname = "";
    @JSONField(ordinal = 52)
    private String server = "";
    @JSONField(ordinal = 53)
    private String port = "";
    @JSONField(ordinal = 54)
    private String ctimeout = "";
    @JSONField(ordinal = 55)
    private String timeout = "";
    @JSONField(ordinal = 56)
    private boolean reUseConnection;
    @JSONField(ordinal = 57)
    private boolean nodelay;
    @JSONField(ordinal = 58)
    private boolean closeConnection;
    @JSONField(ordinal = 59)
    private String soLinger = "";
    @JSONField(ordinal = 60)
    private String eolByte = "";
    @JSONField(ordinal = 61)
    private String request = "";
    @JSONField(ordinal = 62)
    private String username = "";
    @JSONField(ordinal = 63)
    private String password = "";
}
