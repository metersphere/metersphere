package io.metersphere.track.service.utils;

import com.alibaba.fastjson.JSONObject;

public class TestPlanRequestUtil {

    public static void changeStringToBoolean(JSONObject runModeConfig) {
        if (runModeConfig != null) {
            if (runModeConfig.get("onSampleError").equals("true") || runModeConfig.get("onSampleError").equals(true)) {
                runModeConfig.put("onSampleError", true);
            } else {
                runModeConfig.put("onSampleError", false);
            }
            if (runModeConfig.get("runWithinResourcePool").equals("true") || runModeConfig.get("runWithinResourcePool").equals(true)) {
                runModeConfig.put("runWithinResourcePool", true);
            } else {
                runModeConfig.put("runWithinResourcePool", false);
            }
        }
    }
}
