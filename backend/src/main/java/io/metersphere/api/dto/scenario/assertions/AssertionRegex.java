package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionRegex extends AssertionType {
    private String subject;
    private String expression;
    private String description;
    private boolean assumeSuccess;

    public AssertionRegex() {
        setType(AssertionType.REGEX);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(expression);
    }
}
