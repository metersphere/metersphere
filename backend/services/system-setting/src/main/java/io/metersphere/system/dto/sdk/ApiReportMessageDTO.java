package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiReportMessageDTO {
    @Schema(description = "message.domain.id")
    private String id;

    @Schema(description = "message.domain.report.name")
    private String name;

}
