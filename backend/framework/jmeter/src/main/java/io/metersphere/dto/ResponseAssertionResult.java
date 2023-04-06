package io.metersphere.dto;

import lombok.Data;

@Data
public class ResponseAssertionResult {

    private String name;

    private String content;

    private String script;

    private String message;

    private boolean pass;
}
