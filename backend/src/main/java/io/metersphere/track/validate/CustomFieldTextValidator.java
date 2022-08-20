package io.metersphere.track.validate;

import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;

public class CustomFieldTextValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
    }
}
