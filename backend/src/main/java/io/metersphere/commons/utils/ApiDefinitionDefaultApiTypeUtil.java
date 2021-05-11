package io.metersphere.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * 获取用户进入接口定义页面 默认展示的接口类型
 *
 * @author song.tianyang
 * @Date 2021/5/11 10:52 上午
 * @Description
 */
public class ApiDefinitionDefaultApiTypeUtil {
    public static final String HTTP = "HTTP";
    public static final String SQL = "SQL";
    public static final String DUBBO = "DUBBO";
    public static final String TCP = "TCP";

    private static HashMap<String, String> apiTypePerferenceMap;

    public synchronized static void addUserSelectApiType(String userId, String apiType) {
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(apiType)) {
            if (apiTypePerferenceMap == null) {
                apiTypePerferenceMap = new HashMap<>();
            }
            apiTypePerferenceMap.put(userId, apiType);
        }
    }

    public synchronized static String getUserSelectedApiType(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return HTTP;
        } else {
            String selectedApiType = apiTypePerferenceMap.get(userId);
            if (StringUtils.equalsAny(selectedApiType, HTTP, SQL, DUBBO, TCP)) {
                return selectedApiType;
            } else {
                return HTTP;
            }
        }
    }
}
