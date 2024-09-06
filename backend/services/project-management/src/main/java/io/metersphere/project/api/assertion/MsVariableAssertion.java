package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 变量断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("VARIABLE")
public class MsVariableAssertion extends MsAssertion {

    private List<VariableAssertionItem> variableAssertionItems;
    @Data
    public static class VariableAssertionItem {
        /**
         * 是否启用
         */
        private Boolean enable = true;
        /**
         * 变量名
         */
        private String variableName;
        /**
         * 匹配条件
         * 值为 MsAssertionCondition
         */
        @NotBlank
        @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
        private String condition;
        /**
         * 匹配值
         */
        private String expectedValue;
    }
}
