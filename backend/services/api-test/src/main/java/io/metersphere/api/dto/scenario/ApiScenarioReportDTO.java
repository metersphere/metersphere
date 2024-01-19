package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenarioReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiScenarioReportDTO extends ApiScenarioReport {
    @Schema(description = "子节点")
    private List<ApiScenarioReportStepDTO> children;
    @Schema(description = "步骤总数")
    private Integer stepTotal;
}
