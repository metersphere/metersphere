package io.metersphere.api.dto.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

/**
 * 变量断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("VARIABLE")
public class VariableAssertion {

    private List<VariableAssertionItem> variableAssertionItem;
    @Data
    public static class VariableAssertionItem {
        /**
         * 是否启用
         */
        private Boolean enable = true;
        /**
         * 变量名
         */
        private String name;
        /**
         * 匹配条件
         * 值为 MsAssertionCondition
         */
        private String condition;
        /**
         * 匹配值
         */
        private String value;
    }
}
