package io.metersphere.api.utils;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioBlob;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiScenarioUtils {
    public static List<ApiScenarioDetail> parseApiScenarioDetail(List<ApiScenario> apiScenarios, Map<String, ApiScenarioBlob> scenarioBlobMap) {
        List<ApiScenarioDetail> returnList = new ArrayList<>();
        for (ApiScenario apiScenario : apiScenarios) {
            ApiScenarioDetail apiScenarioDetail = BeanUtils.copyBean(new ApiScenarioDetail(), apiScenario);
            apiScenarioDetail.setSteps(List.of());
            ApiScenarioBlob apiScenarioBlob = scenarioBlobMap.get(apiScenario.getId());
            if (apiScenarioBlob != null) {
                apiScenarioDetail.setScenarioConfig(JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class));
            }
            returnList.add(apiScenarioDetail);
        }
        return returnList;
    }
}
