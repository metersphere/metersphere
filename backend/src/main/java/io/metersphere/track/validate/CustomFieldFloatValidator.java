package io.metersphere.track.validate;

import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldFloatValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        try {
            if (StringUtils.isNotBlank(value)) {
                Float.parseFloat(value);
            }
        } catch (Exception e) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_float_tip"), customField.getName()));
        }
    }
}
