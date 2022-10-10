package io.metersphere.api.dto.definition.request.assertions.document;

import lombok.Data;

@Data
public class MsAssertionDocument {
    private boolean enable = true;
    private String type;
    private Document data;
}
