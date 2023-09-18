package io.metersphere.project.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnvAssertionDuration extends EnvAssertionType {
    @Schema(description = "响应时间")
    private long value;

    public EnvAssertionDuration() {
        setType(DURATION);
    }

    public boolean isValid() {
        return value > 0 && isEnable();
    }
}
