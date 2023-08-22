package io.metersphere.sdk.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnvAssertionJsonPath extends EnvAssertionType {
    @Schema(description = "期望值")
    private String expect;
    @Schema(description = "表达式")
    private String expression;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "option 包含/不包含/正则/等于/不等于/大于/小于")
    private String option = "REGEX";

    public EnvAssertionJsonPath() {
        setType(JSON_PATH);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && isEnable();
    }
}
