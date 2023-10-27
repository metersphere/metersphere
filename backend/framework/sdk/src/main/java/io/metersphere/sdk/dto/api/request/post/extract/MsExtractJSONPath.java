package io.metersphere.sdk.dto.api.request.post.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsExtractJSONPath extends MsExtractCommon {
    public MsExtractJSONPath() {
        setType(JSON_PATH);
    }
}
