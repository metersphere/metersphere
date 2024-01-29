package io.metersphere.sdk.dto.api.result;

import lombok.Data;

/**
 * 断言结果
 */
@Data
public class ResponseAssertionResult {

    /**
     * 断言名称
     */
    private String name;

    /**
     * 断言内容
     */
    private String content;

    /**
     * 断言脚本
     */
    private String script;

    /**
     * 断言结果
     */
    private String message;

    /**
     * 是否通过
     */
    private boolean pass;
}
