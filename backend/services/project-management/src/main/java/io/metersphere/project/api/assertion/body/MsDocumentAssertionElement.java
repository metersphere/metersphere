package io.metersphere.project.api.assertion.body;

import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文档断言项
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-23  11:43
 */
@Data
public class MsDocumentAssertionElement {
    private String id;
    /**
     * 参数名
     */
    @Size(max = 100)
    private String paramName;
    /**
     * 必含
     */
    private Boolean include = false;
    /**
     * 类型
     * 取值参考 {@link DocumentAssertionType}
     */
    @EnumValue(enumClass = DocumentAssertionType.class)
    private String type;
    /**
     * 类型校验
     */
    private Boolean typeVerification = false;
    /**
     * 匹配条件
     * 取值参考 {@link MsAssertionCondition}
     */
    @NotBlank
    @EnumValue(enumClass = MsAssertionCondition.class)
    private String condition;
    /**
     * 匹配值
     * 即预期结果
     */
    private Object expectedResult;
    /**
     * 组内校验
     */
    private Boolean arrayVerification = false;
    /**
     * 子对象
     */
    @Valid
    private List<MsDocumentAssertionElement> children;
    /**
     * 在执行时组装数据用
     */
    private String jsonPath;
    /**
     * 分组id
     */
    private String groupId;
    /**
     * 跨行数
     */
    private int rowspan;

    /**
     * 文档断言类型
     */
    public enum DocumentAssertionType implements ValueEnum {
        STRING("string"),
        NUMBER("number"),
        INTEGER("integer"),
        BOOLEAN("boolean"),
        ARRAY("array");

        private String value;

        DocumentAssertionType(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
    public String getJsonPath() {
        //TODO 未实现
        return jsonPath;
    }

}
