package io.metersphere.api.dto.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

/**
 * 响应头断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_HEADER")
public class ResponseHeaderAssertion extends MsAssertion {

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
        private String value;
    }
}
