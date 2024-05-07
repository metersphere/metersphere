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
        if (StringUtils.isBlank(this.condition) || StringUtils.equals(this.condition, ParamConditionEnums.EQUALS.name())) {
            return StringUtils.equals(this.value, value);
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.NOT_EQUALS.name())) {
            return !StringUtils.equals(this.value, value);
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.CONTAINS.name())) {
            return StringUtils.contains(this.value, value);
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_EQUALS.name())) {
            try {
                int length = value.length();
                return this.value.length() == length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_NOT_EQUALS.name())) {
            try {
                int length = value.length();
                return this.value.length() != length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_SHOT.name())) {
            try {
                int length = value.length();
                return this.value.length() < length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_LARGE.name())) {
            try {
                int length = value.length();
                return this.value.length() > length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.REGULAR_MATCH.name())) {
            try {
                return this.value.matches(Pattern.quote(value));
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
