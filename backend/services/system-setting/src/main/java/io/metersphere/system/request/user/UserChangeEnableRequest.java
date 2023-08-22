package io.metersphere.system.request.user;

import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserChangeEnableRequest extends TableBatchProcessDTO {
    @Schema(description = "禁用/启用", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean enable;
}
