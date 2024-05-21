package io.metersphere.api.dto.mockserver;

import io.metersphere.api.constants.mockserver.ParamConditionEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

@Data
public class KeyValueInfo {
    @Schema(description = "Key")
    private String key;
    @Schema(description = "Value")
    private String value;
    /**
     * 默认等于，可选：等于、不等于、长度等于、长度大于、长度小于、包含、不包含、为空、非空、正则匹配
     * 文件类型的参数，等于、不等于、为空、非空
     */
    @Schema(description = "条件")
    private String condition;
    @Schema(description = "描述")
    private String description;

    public boolean matchValue(String value) {
        return switch (ParamConditionEnums.valueOf(this.condition)) {
            case EQUALS -> StringUtils.equals(this.value, value);
            case NOT_EQUALS -> !StringUtils.equals(this.value, value);
            case CONTAINS -> StringUtils.contains(value, this.value);
            case NOT_CONTAINS -> !StringUtils.contains(value, this.value);
            case LENGTH_EQUALS -> this.value.length() == value.length();
            case LENGTH_NOT_EQUALS -> this.value.length() != value.length();
            case LENGTH_SHOT -> value.length() < this.value.length();
            case LENGTH_LARGE -> value.length() > this.value.length();
            case REGULAR_MATCH -> value.matches(Pattern.quote(this.value));
            case IS_EMPTY -> StringUtils.isBlank(value);
            case IS_NOT_EMPTY -> StringUtils.isNotBlank(value);
            default -> false;
        };
    }
}
