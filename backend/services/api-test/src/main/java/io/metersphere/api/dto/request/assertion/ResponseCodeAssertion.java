package io.metersphere.api.dto.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_CODE")
public class ResponseCodeAssertion extends MsAssertion {
    /**
     * 匹配条件
     * 不校验即忽略状态
     * 选择其他条件时，也忽略状态
     * 不校验可搭配其他校验使用
     * 值为 MsAssertionCondition
     */
    private String condition;
    /**
     * 匹配值
     */
    private String value;
}
