package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiReportDTO extends ApiReport {
    @Schema(description = "子节点")
    private List<ApiReportStep> children;

}
