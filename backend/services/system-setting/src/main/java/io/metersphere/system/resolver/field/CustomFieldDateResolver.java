package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.DateUtils;
import io.metersphere.system.dto.CustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldDateResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateUtils.getDate(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }
}
