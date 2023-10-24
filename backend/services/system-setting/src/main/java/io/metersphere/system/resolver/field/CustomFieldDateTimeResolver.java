package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.DateUtils;
import io.metersphere.system.dto.CustomFieldDao;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldDateTimeResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateUtils.getTime(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }

}
