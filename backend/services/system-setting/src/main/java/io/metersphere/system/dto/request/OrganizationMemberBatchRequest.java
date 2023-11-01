package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationMemberBatchRequest extends OrganizationMemberRequest{

    /**
     * 组织ID集合
     */
    @Schema(description =  "组织ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{organization.id.not_blank}")
    private List<String> organizationIds;
}
