package io.metersphere.api.dto.report;

import io.metersphere.api.domain.ApiScenarioReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiScenarioReportListDTO extends ApiScenarioReport {
    @Schema(description = "创建人")
    private String createUserName;
    @Schema(description = "更新人")
    private String updateUserName;

}
