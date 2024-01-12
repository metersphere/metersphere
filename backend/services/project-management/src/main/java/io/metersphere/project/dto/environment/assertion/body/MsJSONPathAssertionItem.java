package io.metersphere.project.dto.environment.assertion.body;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class MsJSONPathAssertionItem extends MsBodyAssertionItem {
    private String expression;
    private String condition;
    private String expectedValue;


    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(expectedValue);
    }
}

