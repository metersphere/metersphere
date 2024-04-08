package io.metersphere.system.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OperationHistoryRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{operation_history.project_id.length_range}")
    private String projectId;

    @Schema(description =  "资源id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.source_id.not_blank}")
    private String sourceId;

    @Schema(description =  "操作人")
    private String createUser;

    @Schema(description =  "操作类型")
    private List<String> types;

    @Schema(description =  "操作模块")
    private String modules;


}
