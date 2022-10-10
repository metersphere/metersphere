package io.metersphere.api.dto.definition.request.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsAssertionXPath2 extends MsAssertionType {
    private String expression;

    public MsAssertionXPath2() {
        setType(MsAssertionType.XPATH2);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && isEnable();
    }
}
