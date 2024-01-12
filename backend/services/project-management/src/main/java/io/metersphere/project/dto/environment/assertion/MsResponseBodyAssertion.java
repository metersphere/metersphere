package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.dto.environment.assertion.body.MsDocumentAssertion;
import io.metersphere.project.dto.environment.assertion.body.MsJSONPathAssertion;
import io.metersphere.project.dto.environment.assertion.body.MsRegexAssertion;
import io.metersphere.project.dto.environment.assertion.body.MsXPathAssertion;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonTypeName("ENV_RESPONSE_BODY")
public class MsResponseBodyAssertion extends MsAssertion {
    /**
     * 断言类型
     * 根据断言类型，选择对应的断言
     * 这里跟前端数据结构有差异
     * 后端从设计层面支持多种断言，前端只支持一种
     * 同时切换可以同时持久化两种类型
     *
     * @see MsBodyAssertionType
     */
    private String assertionBodyType;
    /**
     * jsonPath断言
     */
    private MsJSONPathAssertion jsonPathAssertion;
    /**
     * xpath断言
     */
    private MsXPathAssertion xpathAssertion;
    /**
     * 文档断言
     */
    private MsDocumentAssertion documentAssertion;
    /**
     * 正则断言
     */
    private MsRegexAssertion regexAssertion;

    private static Map<MsBodyAssertionType, Class> bodyAssertionClassMap = new HashMap<>();

    static {
        bodyAssertionClassMap.put(MsBodyAssertionType.JSON_PATH, MsJSONPathAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.DOCUMENT, MsDocumentAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.REGEX, MsRegexAssertion.class);
        bodyAssertionClassMap.put(MsBodyAssertionType.XPATH, MsXPathAssertion.class);
    }

    public Class getBodyAssertionClassByType() {
        return bodyAssertionClassMap.get(MsBodyAssertionType.valueOf(assertionBodyType));
    }

    public Object getBodyAssertionDataByType() {
        Map<MsBodyAssertionType, Object> boadyAssertionMap = new HashMap<>();
        boadyAssertionMap.put(MsBodyAssertionType.JSON_PATH, jsonPathAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.DOCUMENT, documentAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.REGEX, regexAssertion);
        boadyAssertionMap.put(MsBodyAssertionType.XPATH, xpathAssertion);
        return boadyAssertionMap.get(MsBodyAssertionType.valueOf(assertionBodyType));
    }

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
