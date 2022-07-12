package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.automation.ApiScenarioExportJmxDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ScenarioToPerformanceInfoDTO {
    //批量导出时加载jmx的对象
    private List<ApiScenarioExportJmxDTO> scenarioJmxList;
    //单独导出时加载jmx的对象
    private JmxInfoDTO jmxInfoDTO;
    //多个场景批量导出性能测试时
    List<JmxInfoDTO> jmxInfoDTOList;
    //项目-环境id
    private Map<String, List<String>> projectEnvMap;
}
