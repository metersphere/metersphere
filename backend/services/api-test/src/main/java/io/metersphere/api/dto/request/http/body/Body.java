package io.metersphere.api.dto.request.http.body;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class Body {
    /**
     * 当前选择的请求体类型
     * @see BodyType
     * 同时持久化多个类型的请求体
     */
    private String bodyType;
    private NoneBody noneBody;
    private FormDataBody formDataBody;
    private WWWFormBody wwwFormBody;
    private JsonBody jsonBody;
    private XmlBody xmlBody;
    private RawBody rawBody;
    private BinaryBody binaryBody;

    public enum BodyType {
        BINARY,
        FORM_DATA,
        NONE,
        RAW,
        WWW_FORM,
        XML,
        JSON
    }
}
