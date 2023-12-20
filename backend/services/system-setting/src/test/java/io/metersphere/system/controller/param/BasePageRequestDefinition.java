package io.metersphere.system.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

/**
 * BasePageRequest 约束定义
 * @author jianxing
 */
@Data
public class BasePageRequestDefinition {
    @Min(value = 1)
    private int current;

    @Min(value = 5)
    @Max(value = 500)
    private int pageSize;

    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;
}
