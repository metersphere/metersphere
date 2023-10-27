package io.metersphere.system.dto.sdk.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jianxing
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRoleRelationUpdateRequest extends GlobalUserRoleRelationUpdateRequest {

    /**
     * 全局用户组列表不支持给非系统级别的用户组添加用户
     * 所以 GlobalUserRoleRelationUpdateRequest 参数不需要 sourceId
     */
    @Schema(description =  "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role_relation.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role_relation.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;
}
