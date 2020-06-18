package io.metersphere.api.dto.scenario.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractJSONPath extends ExtractCommon {
    public ExtractJSONPath() {
        setType(ExtractType.JSON_PATH);
    }
}
