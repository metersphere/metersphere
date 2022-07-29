package io.metersphere.track.service.utils;

import com.alibaba.fastjson.JSONObject;

public class TestPlanRequestUtil {

    private static final String ON_SAMPLE_ERROR = "onSampleError";
    private static final String RUN_WITHIN_RESOURCE_POOL = "runWithinResourcePool";

    public static void changeStringToBoolean(JSONObject runModeConfig) {
        if (runModeConfig != null) {
            if (runModeConfig.get(ON_SAMPLE_ERROR).equals("true") || runModeConfig.get(ON_SAMPLE_ERROR).equals(true)) {
                runModeConfig.put(ON_SAMPLE_ERROR, true);
            } else {
                runModeConfig.put(ON_SAMPLE_ERROR, false);
            }
            if (runModeConfig.get(RUN_WITHIN_RESOURCE_POOL).equals("true") || runModeConfig.get(RUN_WITHIN_RESOURCE_POOL).equals(true)) {
                runModeConfig.put(RUN_WITHIN_RESOURCE_POOL, true);
            } else {
                runModeConfig.put(RUN_WITHIN_RESOURCE_POOL, false);
            }
        }
    }
}
