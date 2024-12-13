package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.assertion.body.MsDocumentAssertion;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertion;
import io.metersphere.project.api.assertion.body.MsRegexAssertion;
import io.metersphere.project.api.assertion.body.MsXPathAssertion;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求体断言
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_BODY")
public class MsResponseBodyAssertion extends MsAssertion {
    /**
     * 断言类型
     * 根据断言类型，选择对应的断言
     * 这里跟前端数据结构有差异
     * 后端从设计层面支持多种断言，前端只支持一种
     * 同时切换可以同时持久化两种类型
     *
     * 取值参考 {@link MsBodyAssertionType}
     */
    @EnumValue(enumClass = MsBodyAssertionType.class)
    private String assertionBodyType;
    /**
     * jsonPath断言
     */
    @Valid
    private MsJSONPathAssertion jsonPathAssertion;
    /**
     * xpath断言
     */
    @Valid
    private MsXPathAssertion xpathAssertion;
    /**
     * 文档断言
     */
    @Valid
    private MsDocumentAssertion documentAssertion;
    /**
     * 正则断言
     */
    @Valid
    private MsRegexAssertion regexAssertion;

    private static Map<MsBodyAssertionType, Class> bodyAssertionClassMap = new HashMap<>();

    static {
        bodyAssertionClassMap.put(MsBodyAssertionType.JSON_PATH, MsJSONPathAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.DOCUMENT, MsDocumentAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.REGEX, MsRegexAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.XPATH, MsXPathAssertion.class);
    }

    @JsonIgnore
    public Class getBodyAssertionClassByType() {
        return bodyAssertionClassMap.get(MsBodyAssertionType.valueOf(assertionBodyType));
    }

    @JsonIgnore
    public Object getBodyAssertionDataByType() {
        Map<MsBodyAssertionType, Object> boadyAssertionMap = new HashMap<>();
        boadyAssertionMap.put(MsBodyAssertionType.JSON_PATH, jsonPathAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.DOCUMENT, documentAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.REGEX, regexAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.XPATH, xpathAssertion);
        return boadyAssertionMap.get(MsBodyAssertionType.valueOf(assertionBodyType));
    }

    /**
     * 请求体断言类型
     */
    public enum MsBodyAssertionType {
        /**
         * 正则断言
         */
        REGEX,
        /**
         * XPath断言
         */
        XPATH,
        /**
         * JSONPath断言
         */
        JSON_PATH,
        /**
         * 文档断言
         */
        DOCUMENT
    }
}
