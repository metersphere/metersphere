package io.metersphere.validate;

import io.metersphere.commons.utils.JSON;
import io.metersphere.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldMultipleTextValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isNotBlank(value)) {
            try {
                JSON.parseArray(value);
            } catch (Exception e) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), customField.getName()));
            }
        }
    }
}
