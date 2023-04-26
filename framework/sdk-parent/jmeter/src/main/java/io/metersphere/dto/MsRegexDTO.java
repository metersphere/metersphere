package io.metersphere.dto;

import lombok.Data;

@Data
public class MsRegexDTO {
    private String subject;
    private String condition;
    private String value;
    private String errorCode;
    private boolean pass;
}
