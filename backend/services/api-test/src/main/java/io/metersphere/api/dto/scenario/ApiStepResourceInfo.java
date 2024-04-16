package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiStepResourceInfo {
    @Schema(description = "资源ID")
    private String id;
    @Schema(description = "资源编号")
    private Long num;
    @Schema(description = "资源名称")
    private String name;
    @Schema(description = "是否已删除")
    private Boolean delete;
    @Schema(description = "所属项目ID")
    private String projectId;
    @Schema(description = "所属项目名称")
    private String projectName;
}
