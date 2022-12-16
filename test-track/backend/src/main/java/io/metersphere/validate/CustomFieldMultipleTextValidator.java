package io.metersphere.validate;

import io.metersphere.dto.CustomFieldDao;
import io.metersphere.exception.CustomFieldValidateException;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldMultipleTextValidator extends AbstractCustomFieldValidator {

    public CustomFieldMultipleTextValidator() {
        this.isKVOption = true;
    }

    @Override
    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isNotBlank(value)) {
            try {
                parse2Array(customField.getName(), value);
            } catch (Exception e) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), customField.getName()));
            }
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, CustomFieldDao customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return StringUtils.EMPTY;
        }
        return parse2Array(keyOrValuesStr);
    }
}
