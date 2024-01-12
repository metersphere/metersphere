package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("ENV_VARIABLE")
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
        private String condition;
        /**
         * 匹配值
         */
        private String expectedValue;
    }
}
