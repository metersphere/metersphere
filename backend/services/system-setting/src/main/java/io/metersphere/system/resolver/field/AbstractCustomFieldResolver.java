package io.metersphere.system.resolver.field;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.CustomFieldDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static io.metersphere.system.controller.handler.result.CommonResultCode.FIELD_VALIDATE_ERROR;

public abstract class AbstractCustomFieldResolver {

    /**
     * 校验参数是否合法
     *
     * @param customField
     * @param value
     */
    abstract public void validate(CustomFieldDTO customField, Object value);

    protected void throwValidateException(String name) {
        throw new MSException(FIELD_VALIDATE_ERROR, Translator.getWithArgs(FIELD_VALIDATE_ERROR.getMessage(), name));
    }

    protected void validateRequired(CustomFieldDTO customField, Object value) {
        if (!customField.getRequired()) {
            return;
        }
        if (value == null) {
            throwValidateException(customField.getName());
        } else if (value instanceof String && StringUtils.isBlank(value.toString())) {
            throwValidateException(customField.getName());
        }
    }

    protected void validateArrayRequired(CustomFieldDTO customField, Object value) {
        if (!customField.getRequired()) {
            return;
        }
        if (value == null || (value instanceof List &&  CollectionUtils.isEmpty((List) value))) {
            throwValidateException(customField.getName());
        }
    }

    protected void validateArray(String name, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof List) {
            ((List) value).forEach(v -> validateString(name, v));
        } else {
            throwValidateException(name);
        }
    }

    protected void validateString(String name, Object v) {
        if (v != null && !(v instanceof String)) {
            throwValidateException(name);
        }
    }

    public Object parse2Value(String value) {
        return value;
    }

    public String parse2String(Object value) {
        return value == null ? null : value.toString();
    }

    protected Object parse2Array(String value) {
        return value == null ? null : JSON.parseArray(value);
    }
}
