package io.metersphere.sdk.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnvAssertionXPath extends EnvAssertionType {
    @Schema(description = "表达式")
    private String expression;

    public EnvAssertionXPath() {
        setType(XPATH2);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && isEnable();
    }
}
