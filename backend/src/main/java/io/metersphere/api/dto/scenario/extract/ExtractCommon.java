package io.metersphere.api.dto.scenario.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractCommon extends ExtractType {
    private String variable;
    private String value; // value: ${variable}
    private String expression;
    private String description;
    private boolean multipleMatching;

    public boolean isValid() {
        return StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(expression);
    }
}
