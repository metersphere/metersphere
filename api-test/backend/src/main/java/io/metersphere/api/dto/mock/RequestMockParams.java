package io.metersphere.api.dto.mock;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author song.tianyang
 * @Date 2021/10/14 4:32 下午
 */
@Getter
@Setter
public class RequestMockParams {
    private JSONObject restParamsObj;
    private JSONObject queryParamsObj;
    private JSONArray bodyParams;

    public JSONObject getParamsObj() {
        JSONObject returnObj = new JSONObject();
        if (restParamsObj != null) {
            for (String key : restParamsObj.keySet()) {
                Object value = restParamsObj.get(key);
                returnObj.put(key, value);
            }
        }
        if (queryParamsObj != null) {
            for (String key : queryParamsObj.keySet()) {
                Object value = queryParamsObj.get(key);
                returnObj.put(key, value);
            }
        }
        return returnObj;
    }

    public boolean isEmpty() {
        return (restParamsObj == null || restParamsObj == null) &&
                (queryParamsObj == null || queryParamsObj == null) &&
                (bodyParams == null || bodyParams == null);
    }
}
