package io.metersphere.api.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "报告ID")
    private String id;
    @Schema(description = "资源池id")
    private String poolId;
    @Schema(description = "报告名称")
    private String name;
    @Schema(description = "项目id")
    private String projectId;
    @Schema(description = "组织id")
    private String organizationId;


}
