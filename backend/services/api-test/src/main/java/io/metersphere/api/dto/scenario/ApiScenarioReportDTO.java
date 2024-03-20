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
    @Schema(description = "控制台信息")
    private String console;
    @Schema(description = "资源池名称")
    private String poolName;
    @Schema(description = "环境名称")
    private String environmentName;
    @Schema(description = "创建人")
    private String creatUserName;

}
