package io.metersphere.sdk.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnvAssertionRegex extends EnvAssertionType {
    @Schema(description = "断言对象  Response Headers/Response Code/Response Data")
    private String subject;
    @Schema(description = "表达式")
    private String expression;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "忽略状态")
    private Boolean assumeSuccess = false;

    public EnvAssertionRegex() {
        setType(EnvAssertionType.REGEX);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(expression) && isEnable();
    }
}
