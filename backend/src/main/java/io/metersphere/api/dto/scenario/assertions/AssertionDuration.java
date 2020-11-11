package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionDuration extends AssertionType {
    private long value;

    public AssertionDuration() {
        setType(AssertionType.DURATION);
    }

    public boolean isValid() {
        return value > 0;
    }
}
