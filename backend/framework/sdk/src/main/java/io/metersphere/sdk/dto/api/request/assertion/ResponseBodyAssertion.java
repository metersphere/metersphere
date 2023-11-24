package io.metersphere.sdk.dto.api.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.dto.api.request.assertion.body.DocumentAssertion;
import io.metersphere.sdk.dto.api.request.assertion.body.JSONPathAssertion;
import io.metersphere.sdk.dto.api.request.assertion.body.RegexAssertion;
import io.metersphere.sdk.dto.api.request.assertion.body.XPathAssertion;
import lombok.Data;

/**
 * 请求体断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_BODY")
public class ResponseBodyAssertion extends MsAssertion {
    /**
     * 断言类型
     * 根据断言类型，选择对应的断言
     * 这里跟前端数据结构有差异
     * 后端从设计层面支持多种断言，前端只支持一种
     * 同时切换可以同时持久化两种类型
     * 值为 MsBodyAssertionType
     */
    private String assertionType;
    /**
     * jsonPath断言
     */
    private JSONPathAssertion jsonPathAssertion;
    /**
     * xpath断言
     */
    private XPathAssertion xpathAssertion;
    /**
     * 文档断言
     */
    private DocumentAssertion documentAssertion;
    /**
     * 正则断言
     */
    private RegexAssertion regexAssertion;
}
