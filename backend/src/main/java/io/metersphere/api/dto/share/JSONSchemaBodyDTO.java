package io.metersphere.api.dto.share;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JSONSchemaBodyDTO {
    private Object jsonSchema;
    private String raw;
}
