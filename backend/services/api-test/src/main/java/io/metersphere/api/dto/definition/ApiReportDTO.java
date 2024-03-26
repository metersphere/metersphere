package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiReportDTO extends ApiReport {
    @Schema(description = "子节点")
    private List<ApiReportStepDTO> children;
    @Schema(description = "控制台信息")
    private String console;
    @Schema(description = "资源池名称")
    private String poolName;
    @Schema(description = "环境名称")
    private String environmentName;
    @Schema(description = "创建人")
    private String creatUserName;
    @Schema(description = "步骤总数")
    private Long total;

}
