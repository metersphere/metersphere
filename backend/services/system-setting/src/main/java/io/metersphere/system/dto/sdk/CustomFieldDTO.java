package io.metersphere.system.dto.sdk;

import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldOption;
import lombok.Data;

import java.util.List;

@Data
public class CustomFieldDTO extends CustomField {
    private List<CustomFieldOption> options;
    /**
     * 是否被模板使用
     */
    private Boolean used = false;
    /**
     * 模板中该字段是否必选
     */
    private Boolean templateRequired = false;
    /**
     * 内置字段的 key
     */
    private String internalFieldKey;
}
