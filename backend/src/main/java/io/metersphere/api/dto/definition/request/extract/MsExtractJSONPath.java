package io.metersphere.api.dto.definition.request.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsExtractJSONPath extends MsExtractCommon {
    public MsExtractJSONPath() {
        setType(MsExtractType.JSON_PATH);
    }
}
