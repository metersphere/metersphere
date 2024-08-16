package io.metersphere.system.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PersonalLocaleRequest {
    @Schema(description = "国际化", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "(zh-CN)|(en-US)", message = "locale格式不正确")
    @NotEmpty
    private String language;
}
