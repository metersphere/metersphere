package io.metersphere.api.dto.schema;


import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * json-schema 参数项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonSchemaItem {
    /**
     * 参数ID
     */
    private String id;
    /**
     * 参数名称
     */
    private String title;
    /**
     * 示例值
     * 这里需要支持填写mock函数
     * 类型为 String
     */
    private String example;
    /**
     * 参数类型
     * 取值范围，参考 {@link JsonSchemaItemType}
     * 默认为 string
     */
    @EnumValue(enumClass = JsonSchemaItemType.class)
    private String type = JsonSchemaItemType.STRING.value;
    /**
     * 参数描述
     */
    private String description;
    /**
     * 子级参数
     * 当 type 为array 时，使用该值
     */
    @Valid
    private List<JsonSchemaItem> items;
    /**
     * 参数属性
     * 当 type 为 object 时，使用该值
     */
    private Map<String, JsonSchemaItem> properties;
    /**
     * 附加属性
     * 包含未在属性列表中定义的额外属性的选项
     * 当 type 为 object 时，使用该值
     */
    private JsonSchemaItem additionalProperties;
    /**
     * 必填参数 这里的值是参数的title
     */
    private List<String> required;
    /**
     * 默认值
     */
    private Object defaultValue;
    /**
     * 参数值需满足的正则表达式
     */
    private String pattern;
    /**
     * 最大长度
     */
    private Integer maxLength;
    /**
     * 最小长度
     */
    private Integer minLength;
    /**
     * 最小值
     */
    private BigDecimal minimum;
    /**
     * 最大值
     */
    private BigDecimal maximum;
    /**
     * 数组最大长度
     */
    private Integer maxItems;
    /**
     * 数组最小长度
     */
    private Integer minItems;
    /**
     * 一般是选择日期格式
     */
    private String format;
    /**
     * 参数值的枚举
     */
    private List<String> enumValues;
    /**
     * 是否启用
     */
    private Boolean enable = true;

    public JsonSchemaItem(String type) {
        this.type = type;
        this.initParam(type);
    }

    public void setType(String type) {
        this.type = type;
        this.initParam(type);
    }

    private void initParam(String type) {
        if (type.equals(PropertyConstant.OBJECT)) {
            this.properties = new LinkedHashMap<>();
        } else if (type.equals(PropertyConstant.ARRAY)) {
            this.items = List.of();
        }
    }

    /**
     * json-schema 参数类型
     */
    enum JsonSchemaItemType implements ValueEnum {
        STRING("string"),
        NUMBER("number"),
        INTEGER("integer"),
        BOOLEAN("boolean"),
        OBJECT("object"),
        ARRAY("array"),
        NULL("null");

        private String value;

        JsonSchemaItemType(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
