package io.metersphere.functional.excel.validate;

import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;

/**
 * @author wx
 */
public class CustomFieldTextValidator extends AbstractCustomFieldValidator {

    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
    }
}
