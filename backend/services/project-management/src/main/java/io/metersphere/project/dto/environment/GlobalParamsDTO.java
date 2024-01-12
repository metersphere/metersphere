package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GlobalParamsDTO implements Serializable {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "全局参数")
    private GlobalParams globalParams;


    @Serial
    private static final long serialVersionUID = 1L;
}
