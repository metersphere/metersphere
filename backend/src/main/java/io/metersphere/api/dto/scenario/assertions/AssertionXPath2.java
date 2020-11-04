package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionXPath2 extends AssertionType {
    private String expression;
    public AssertionXPath2() {
        setType(AssertionType.XPATH2);
    }
}
