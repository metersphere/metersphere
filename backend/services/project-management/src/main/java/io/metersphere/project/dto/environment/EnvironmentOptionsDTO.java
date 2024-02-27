package io.metersphere.project.dto.environment;

import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.sdk.domain.Environment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class EnvironmentOptionsDTO extends Environment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    private String id;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
    @Schema(description = "环境名称")
    private String name;
    @Schema(description = "域名")
    private List<HttpConfig> domain;
    @Schema(description = "是否是mock环境")
    private Boolean mock;
    @Schema(description = "描述")
    private String description;

}
