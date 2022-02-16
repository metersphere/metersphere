package io.metersphere.api.dto.definition.request.controller.loop;

import lombok.Data;

@Data
public class MsWhileController {
    private String variable;
    private String operator;
    private String value;
    private long timeout;
    private Object requestResult;
}
