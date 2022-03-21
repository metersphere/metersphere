package io.metersphere.api.dto.definition.request.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsAssertionDuration extends MsAssertionType {
    private long value;

    public MsAssertionDuration() {
        setType(MsAssertionType.DURATION);
    }

    public boolean isValid() {
        return value > 0 && isEnable();
    }
}
