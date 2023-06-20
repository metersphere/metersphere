package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserEditEnableRequest {
    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.not.empty}")
    List<String> userIdList;
    
    boolean enable;
}
