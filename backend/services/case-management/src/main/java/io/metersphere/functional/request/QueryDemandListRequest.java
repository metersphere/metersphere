package io.metersphere.functional.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryDemandListRequest extends BasePageRequest {
    @Schema(description = "功能用例id")
    @NotBlank(message = "{demand_request.case_id.not_blank}")
    private String caseId;

    @Schema(description = "当前项目id")
    @NotBlank(message = "{demand_request.project_id.not_blank}")
    private String projectId;
}
