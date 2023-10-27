package io.metersphere.sdk.dto.api.request.assertion;

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
