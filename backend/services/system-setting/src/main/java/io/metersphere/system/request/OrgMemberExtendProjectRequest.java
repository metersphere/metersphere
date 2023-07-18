package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class OrgMemberExtendProjectRequest extends OrganizationMemberRequest{

    /**
     * 项目ID集合
     */
    @Schema(title = "项目ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{projectIds.not_empty}")
    private List<String> projectIds;

}
