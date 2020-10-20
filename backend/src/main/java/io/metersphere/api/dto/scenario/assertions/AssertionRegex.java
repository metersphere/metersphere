package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionRegex extends AssertionType {
    private String subject;
    private String expression;
    private String description;
    private Boolean assumeSuccess;

    public AssertionRegex() {
        setType(AssertionType.REGEX);
    }
}
