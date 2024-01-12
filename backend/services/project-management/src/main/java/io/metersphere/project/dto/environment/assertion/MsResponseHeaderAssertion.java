package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("ENV_RESPONSE_HEADER")
public class MsResponseHeaderAssertion extends MsAssertion {

    private List<ResponseHeaderAssertionItem> assertions;


    @Data
    public static class ResponseHeaderAssertionItem {
        /**
         * 是否启用
         */
        private Boolean enable = true;
        /**
         * 响应头
         */
        private String header;
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
