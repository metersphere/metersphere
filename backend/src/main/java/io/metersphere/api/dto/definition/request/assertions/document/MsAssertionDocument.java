package io.metersphere.api.dto.definition.request.assertions.document;

import lombok.Data;

@Data
public class MsAssertionDocument {
    private String type;
    private Document data;
}
