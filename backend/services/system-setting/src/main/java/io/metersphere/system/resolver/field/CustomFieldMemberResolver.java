package io.metersphere.system.resolver.field;


import io.metersphere.system.dto.CustomFieldDTO;

public class CustomFieldMemberResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
    }
}
