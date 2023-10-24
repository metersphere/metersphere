package io.metersphere.system.resolver.field;

import io.metersphere.system.dto.CustomFieldDao;

public class CustomFieldIntegerResolver extends AbstractCustomFieldResolver {
    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        if (value != null && !(value instanceof Integer)) {
            throwValidateException(customField.getName());
        }
    }

    @Override
    public Object parse2Value(String value) {
        return value == null ? null : Integer.parseInt(value);
    }
}
