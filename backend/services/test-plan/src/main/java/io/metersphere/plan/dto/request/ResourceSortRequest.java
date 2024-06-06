package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.request.PosRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceSortRequest extends PosRequest {
    @Schema(description = "测试集ID")
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testCollectionId;
}

