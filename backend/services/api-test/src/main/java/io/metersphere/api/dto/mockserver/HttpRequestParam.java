package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.sdk.util.XMLUtils;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class HttpRequestParam {

    private boolean isPost;

    private LinkedHashMap<String, String> restParams;

    //form-data的kv类型参数也存储在queryParamObj中
    private LinkedHashMap<String, String> queryParamsObj;

    private LinkedHashMap<String, String> bodyParamsObj;

    private byte[] binaryParamsObj;

    private String paramType;

    //JSONArray 或 JSONObject
    private String jsonString;

    private Map<String, Object> xmlToJsonParam;

    private String raw;

    public void setXmlParam(String xmlParam) {
        this.setParamType(Body.BodyType.XML.name());
        this.setRaw(xmlParam);
        Map<String, Object> jsonMap = XMLUtils.xmlStringToJson(xmlParam);
        this.setXmlToJsonParam(jsonMap);
    }

    public void setJsonParam(String requestPostString) {
        this.setParamType(Body.BodyType.JSON.name());
        this.jsonString = requestPostString;
    }
}
