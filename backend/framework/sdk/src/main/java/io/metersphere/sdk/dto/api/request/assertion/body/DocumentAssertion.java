package io.metersphere.sdk.dto.api.request.assertion.body;

import lombok.Data;

/**
 * 文档断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:19
 */
@Data
public class DocumentAssertion extends BodyAssertionItem {
    /**
     * 跟随定义的apiId
     * 传空为不跟随接口定义
     */
    private String followApiId;
    /**
     * 文档类型
     * json 或者 xml
     * 根据文档类型，选择对应的文档
     * 这里跟前端数据结构有差异
     * 后端从设计层面支持多种文档格式，前端只支持一种
     * 同时切换可以同时持久化两种格式
     */
    private String documentType;
    /**
     * json格式的文档断言
     */
    private DocumentAssertionElement jsonAssertion;
    /**
     * xml格式的文档断言
     */
    private DocumentAssertionElement xmlAssertion;
}
