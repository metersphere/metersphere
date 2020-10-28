package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionJSR223 extends AssertionType {
    private String variable;
    private String operator;
    private String value;
    private String desc;
    private String name;
    private String script;
    private String language;

    public AssertionJSR223() {
        setType(AssertionType.JSR223);
    }
}
