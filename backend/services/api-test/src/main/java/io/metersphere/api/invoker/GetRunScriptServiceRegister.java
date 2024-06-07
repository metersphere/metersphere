package io.metersphere.api.invoker;

import io.metersphere.api.service.GetRunScriptService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:48
 */
public class GetRunScriptServiceRegister {

    private static final Map<ApiExecuteResourceType, GetRunScriptService> getRunScriptServiceMap = new HashMap<>();


    public static void register(ApiExecuteResourceType apiExecuteResourceType, GetRunScriptService getRunScriptService) {
        getRunScriptServiceMap.put(apiExecuteResourceType, getRunScriptService);
    }

    public static GetRunScriptService getRunScriptService(ApiExecuteResourceType apiExecuteResourceType) {
        return getRunScriptServiceMap.get(apiExecuteResourceType);
    }
}
