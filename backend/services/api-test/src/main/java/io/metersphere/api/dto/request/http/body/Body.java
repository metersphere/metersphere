package io.metersphere.api.dto.request.http.body;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<BodyType, Class> bodyTypeClassMap = new HashMap<>();

    static {
        bodyTypeClassMap.put(BodyType.NONE, NoneBody.class);
        bodyTypeClassMap.put(BodyType.FORM_DATA, FormDataBody.class);
        bodyTypeClassMap.put(BodyType.WWW_FORM, WWWFormBody.class);
        bodyTypeClassMap.put(BodyType.JSON, JsonBody.class);
        bodyTypeClassMap.put(BodyType.XML, XmlBody.class);
        bodyTypeClassMap.put(BodyType.RAW, RawBody.class);
        bodyTypeClassMap.put(BodyType.BINARY, BinaryBody.class);
    }

    public Class getBodyClassByType() {
        return bodyTypeClassMap.get(BodyType.valueOf(bodyType));
    }

    public Object getBodyDataByType() {
        Map<BodyType, Object> boadyDataMap = new HashMap<>();
        boadyDataMap.put(BodyType.NONE, noneBody);
        boadyDataMap.put(BodyType.FORM_DATA, formDataBody);
        boadyDataMap.put(BodyType.WWW_FORM, wwwFormBody);
        boadyDataMap.put(BodyType.JSON, jsonBody);
        boadyDataMap.put(BodyType.XML, xmlBody);
        boadyDataMap.put(BodyType.RAW, rawBody);
        boadyDataMap.put(BodyType.BINARY, binaryBody);
        return boadyDataMap.get(BodyType.valueOf(bodyType));
    }


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
