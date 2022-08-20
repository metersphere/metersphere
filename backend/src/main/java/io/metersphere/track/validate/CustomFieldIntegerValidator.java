package io.metersphere.track.validate;

import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldIntegerValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        try {
            if (StringUtils.isNotBlank(value)) {
                Integer.parseInt(value);
            }
        } catch (Exception e) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_int_tip"), customField.getName()));
        }
    }
}
