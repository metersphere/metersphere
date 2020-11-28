package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

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

    public boolean isValid() {
        return StringUtils.isNotBlank(script) && StringUtils.isNotBlank(language);
    }
}
