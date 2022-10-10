package io.metersphere.validate;

import io.metersphere.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;

public class CustomFieldTextValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
    }
}
