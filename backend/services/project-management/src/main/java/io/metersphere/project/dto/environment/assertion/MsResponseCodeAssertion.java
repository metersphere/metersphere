package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("ENV_RESPONSE_CODE")
public class MsResponseCodeAssertion extends MsAssertion {
    /**
     * 匹配条件
     * 不校验即忽略状态
     * 选择其他条件时，也忽略状态
     * 不校验可搭配其他校验使用
     *
     * @see io.metersphere.sdk.constants.MsAssertionCondition
     */
    private String condition;
    /**
     * 匹配值
     */
    private String expectedValue;
}
