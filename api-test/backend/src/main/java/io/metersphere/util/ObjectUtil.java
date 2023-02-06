package io.metersphere.util;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Map;

public class ObjectUtil {
    public static JSONObject parseObject(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                MSException.throwException("value is null");
            }
            Map<String, Object> map = JSON.parseObject(value, Map.class);
            return new JSONObject(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
