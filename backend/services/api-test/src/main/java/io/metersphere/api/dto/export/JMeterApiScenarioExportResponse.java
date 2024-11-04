package io.metersphere.api.dto.export;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wx
 */
@Data
public class JMeterApiScenarioExportResponse extends ApiScenarioExportResponse {
    Map<String, String> scenarioJmxMap = new HashMap<>();

    public void addJmx(String key, String jmx) {
        scenarioJmxMap.put(key, jmx);
    }
}
