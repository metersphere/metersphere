package io.metersphere.api.dto.scenario.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractRegex extends ExtractCommon {
    private String useHeaders;

    public ExtractRegex() {
        setType(ExtractType.REGEX);
    }
}
