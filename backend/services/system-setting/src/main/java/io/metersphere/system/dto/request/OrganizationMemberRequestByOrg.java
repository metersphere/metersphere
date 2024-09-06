package io.metersphere.system.dto.request;

import io.metersphere.sdk.dto.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author guoyuqi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationMemberRequestByOrg extends BaseCondition {

    /**
     * 组织ID
     */
    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    private String organizationId;
    /**
     * 成员ID集合
     */
    @Schema(description =  "成员ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> memberIds;

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "不处理的ID")
    private List<String> excludeIds;
}
