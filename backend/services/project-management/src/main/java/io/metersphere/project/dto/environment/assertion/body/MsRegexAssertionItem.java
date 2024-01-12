package io.metersphere.project.dto.environment.assertion.body;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class MsRegexAssertionItem extends MsBodyAssertionItem {
    private String expression;

    public boolean isValid() {
        return StringUtils.isNotBlank(expression);
    }
}