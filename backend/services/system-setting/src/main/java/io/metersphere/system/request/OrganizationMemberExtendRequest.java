package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


/**
 * @author guoyuqi
 */
@Data
public class OrganizationMemberExtendRequest extends OrganizationMemberRequest {

    /**
     * 用户组ID集合
     */
    @Schema(title = "用户组ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{userGroupId.id.not_null}")
    private List<String> userGroupIds;
}
