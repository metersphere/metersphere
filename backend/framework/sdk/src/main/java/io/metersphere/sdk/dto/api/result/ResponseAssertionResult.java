package io.metersphere.sdk.dto.api.result;

import lombok.Data;

/**
 * 断言结果
 */
@Data
public class ResponseAssertionResult {

    private String name;

    private String content;

    private String script;

    private String message;

    private boolean pass;
}
