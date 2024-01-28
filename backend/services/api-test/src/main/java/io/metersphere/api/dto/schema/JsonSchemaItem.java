package io.metersphere.api.dto.schema;


import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.system.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * json-schema 参数项
 */
@Data
public class JsonSchemaItem {
    /**
     * 示例
     */
    private Object example;
    /**
     * 参数ID
     */
    @NotBlank
    @Size(max = 50)
    private String id;
    /**
     * 参数名称
     */
    @Size(max = 200)
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
     * 当 type 为 object 或者 array 时，使用该值
     */
    @Valid
    private JsonSchemaItem items;
    private Map<String, Object> mock;
    private Map<String, JsonSchemaItem> properties;
    private JsonSchemaItem additionalProperties;
    private List<String> required;
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
    private String schema;
    private String format;
    private List<String> enumString;
    private List<Number> enumInteger;
    private List<BigDecimal> enumNumber;
    private Map<String, Object> extensions = null;


    public JsonSchemaItem() {
        this.mock = new LinkedHashMap<>();
        this.mock.put("mock", "");
    }

    public JsonSchemaItem(String type) {
        this.type = type;
        this.initParam(type);
    }

    public void setType(String type) {
        this.type = type;
        this.initParam(type);
    }

    private void initParam(String type) {
        if (type.equals("object")) {
            this.properties = new LinkedHashMap<>();
        } else if (type.equals("array")) {
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
