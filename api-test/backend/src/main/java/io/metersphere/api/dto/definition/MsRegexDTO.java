package io.metersphere.api.dto.definition;

import lombok.Data;

@Data
public class MsRegexDTO {
    private String subject;
    private String condition;
    private String value;
    private String errorCode;
    private boolean pass;
}
