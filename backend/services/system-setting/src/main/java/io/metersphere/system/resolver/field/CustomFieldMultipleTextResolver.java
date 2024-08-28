package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.CustomFieldDTO;

public class CustomFieldMultipleTextResolver extends AbstractCustomFieldResolver {


    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateArrayRequired(customField, value);
        validateArray(customField.getName(), value);
    }

    @Override
    public String parse2String(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public Object parse2Value(String value) {
        return parse2Array(value);
    }
}
