package io.metersphere.system.dto;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "user_key ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.id.not_blank}", groups = {Updated.class})
    private String id;
    @Schema(description = "是否永久有效", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean forever;
    @Schema(description = "到期时间")
    private Long expireTime;
    @Schema(description = "描述")
    private String description;

}