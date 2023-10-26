package io.metersphere.api.dto.request.post.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsExtractXPath extends MsExtractCommon {
    public MsExtractXPath() {
        setType(MsExtractType.XPATH);
    }
}
