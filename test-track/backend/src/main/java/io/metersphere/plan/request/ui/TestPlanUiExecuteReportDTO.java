package io.metersphere.plan.request.ui;

import io.metersphere.dto.TestPlanUiScenarioDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanUiExecuteReportDTO {
    private Map<String, String> testPlanUiScenarioIdAndReportIdMap;
    private Map<String, TestPlanUiScenarioDTO> uiScenarioInfoDTOMap;
}
