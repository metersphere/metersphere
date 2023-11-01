package io.metersphere.system.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationProjectRequest extends BasePageRequest {
    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}")
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}")
    private String organizationId;
}
