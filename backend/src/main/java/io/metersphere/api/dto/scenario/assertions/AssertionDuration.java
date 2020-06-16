package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssertionDuration extends AssertionType {
    private long value;

    public AssertionDuration() {
        setType(AssertionType.DURATION);
    }
}
