package io.metersphere.project.dto.environment.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommonParams implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "链接超时时间")
    private Long requestTimeout = 60000L;
    @Schema(description = "响应超时时间")
    private Long responseTimeout = 60000L;
}
