package io.metersphere.sdk.dto.api.request.assertion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsAssertionJSR223 extends MsAssertionType {
    private String variable;
    private String operator;
    private String value;
    private String desc;
    private String name;
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

    public MsAssertionJSR223() {
        setType(MsAssertionType.JSR223);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(script) && StringUtils.isNotBlank(scriptLanguage) && isEnable();
    }
}
