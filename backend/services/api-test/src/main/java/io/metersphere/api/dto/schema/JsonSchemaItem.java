package io.metersphere.api.dto.schema;


import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.system.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
     * 示例值  类似于 hangman 或者 @string 如果是mock 也是用这个属性
     */
    private Object example;
    /**
     * 参数ID
     */
    @NotBlank
    private String id;
    /**
     * 参数名称
     */
    private String title;
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
    private JsonSchemaItem items;
    /**
     * 参数属性
     * 当 type 为 object 时，使用该值
     */
    private Map<String, JsonSchemaItem> properties;
    /**
     * 附加属性
     * 当 type 为 object 时，使用该值
     */
    private JsonSchemaItem additionalProperties;
    /**
     * 必填参数 这里的值是参数的title
     */
    private List<String> required;
    /**
     * 正则表达式
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
     * schema 一般是解析用的
     */
    private String schema;
    /**
     * 一般是选择日期格式
     */
    private String format;
    /**
     * 字符串的枚举
     */
    private List<String> enumString;
    /**
     * 整数的枚举
     */
    private List<Number> enumInteger;
    /**
     * 数字的枚举
     */
    private List<BigDecimal> enumNumber;
    /**
     * 延伸 一般是用来存放一些自定义的属性
     */
    private Map<String, Object> extensions = null;

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
            this.items = new JsonSchemaItem();
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
