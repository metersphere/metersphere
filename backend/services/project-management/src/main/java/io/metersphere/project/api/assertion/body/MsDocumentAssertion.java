package io.metersphere.project.api.assertion.body;

import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文档断言
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:19
 */
@Data
public class MsDocumentAssertion extends MsBodyAssertionItem {
    /**
     * 跟随定义的apiId
     * 传空为不跟随接口定义
     * 如果选择了
     */
    @Size(max = 50)
    private String followApiId;
    /**
     * 文档类型
     * json 或者 xml
     * 根据文档类型，选择对应的文档
     * 这里跟前端数据结构有差异
     * 后端从设计层面支持多种文档格式，前端只支持一种
     * 同时切换可以同时持久化两种格式
     * <p>
     * 取值参考 {@link DocumentType}
     * =
     */
    @EnumValue(enumClass = DocumentType.class)
    private String documentType;
    /**
     * json格式的文档断言
     */
    @Valid
    private MsDocumentAssertionElement jsonAssertion;
    /**
     * xml格式的文档断言
     */
    @Valid
    private MsDocumentAssertionElement xmlAssertion;

    public enum DocumentType {
        JSON,
        XML;
    }
}
