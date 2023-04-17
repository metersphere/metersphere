package io.metersphere.api.dto.mock;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author song.tianyang
 * @Date 2021/10/14 4:32 下午
 */
@Getter
@Setter
public class RequestMockParams {
    private boolean isPost;
    private String paramType;
    private JSONObject restParamsObj;

    //form-data的kv类型参数也存储在queryParamObj中
    private JSONObject queryParamsObj;

    //JSONArray 或 JSONObject
    private Object jsonParam;

    private JSONObject xmlToJsonParam;

    private String raw;

    private String tcpParam;


    public boolean isEmpty() {
        boolean isJsonParamEmpty = false;
        if (jsonParam instanceof JSONObject) {
            isJsonParamEmpty = ((JSONObject) jsonParam).isEmpty();
        } else if (jsonParam instanceof JSONArray) {
            isJsonParamEmpty = ((JSONArray) jsonParam).isEmpty();
        } else {
            isJsonParamEmpty = jsonParam == null;
        }
        if (isPost) {
            return (restParamsObj == null || restParamsObj.isEmpty()) &&
                    isJsonParamEmpty &&
                    (queryParamsObj == null || queryParamsObj.isEmpty())
                    && StringUtils.isBlank(raw);
        } else {
            return (restParamsObj == null || restParamsObj.isEmpty()) && isJsonParamEmpty &&
                    (queryParamsObj == null || queryParamsObj.isEmpty());
        }

    }
}
