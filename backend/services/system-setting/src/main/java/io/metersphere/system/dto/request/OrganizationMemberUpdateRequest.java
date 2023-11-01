package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class OrganizationMemberUpdateRequest implements Serializable {
    /**
     * 组织ID
     */
    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    private String organizationId;

    /**
     * 成员ID
     */
    @Schema(description =  "成员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.member.not_blank}")
    private String memberId;

    /**
     * 用户组ID集合
     */
    @Schema(description =  "用户组ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{userGroupId.id.not_null}")
    private List<String> userRoleIds;


    /**
     * 项目ID集合
     */
    @Schema(description =  "项目ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> projectIds;
}
