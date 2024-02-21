package io.metersphere.api.dto.mockserver;

import io.metersphere.api.constants.mockserver.ParamConditionEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class KeyValueInfo {
    @Schema(description = "Key")
    private String key;
    @Schema(description = "Value")
    private String value;
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
                int length = Integer.parseInt(value);
                return this.value.length() == length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_NOT_EQUALS.name())) {
            try {
                int length = Integer.parseInt(value);
                return this.value.length() != length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_SHOT.name())) {
            try {
                int length = Integer.parseInt(value);
                return this.value.length() < length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.LENGTH_LARGE.name())) {
            try {
                int length = Integer.parseInt(value);
                return this.value.length() > length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(this.condition, ParamConditionEnums.REGULAR_MATCH.name())) {
            try {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(this.value);
                boolean isMatch = matcher.matches();
                return isMatch;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
