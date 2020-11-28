package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionJsonPath extends AssertionType {
    private String expect;
    private String expression;
    private String description;

    public AssertionJsonPath() {
        setType(AssertionType.JSON_PATH);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(expression);
    }
}
