package io.metersphere.system.resolver.field;


import io.metersphere.system.dto.CustomFieldDao;

public class CustomFieldMemberResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
    }
}
