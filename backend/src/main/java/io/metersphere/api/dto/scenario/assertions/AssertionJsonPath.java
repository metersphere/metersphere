package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionJsonPath extends AssertionType {
    private String expect;
    private String expression;
    private String description;

    public AssertionJsonPath() {
        setType(AssertionType.JSON_PATH);
    }
}
