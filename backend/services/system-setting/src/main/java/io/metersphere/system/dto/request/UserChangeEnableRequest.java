package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserChangeEnableRequest extends UserBaseBatchRequest {
    @Schema(description = "禁用/启用", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean enable;
}
