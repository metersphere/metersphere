package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import io.metersphere.sdk.dto.BasePageRequest;

/**
 * @author : jianxing
 * @date : 2023-6-12
 */
@Getter
@Setter
public class GlobalUserRoleRelationQueryRequest extends BasePageRequest {
    @NotBlank
    @Schema(description =  "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleId;
}
