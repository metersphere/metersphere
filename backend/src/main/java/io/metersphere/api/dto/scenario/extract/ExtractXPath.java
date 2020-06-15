package io.metersphere.api.dto.scenario.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractXPath extends ExtractCommon {
    public ExtractXPath() {
        setType(ExtractType.XPATH);
    }
}
