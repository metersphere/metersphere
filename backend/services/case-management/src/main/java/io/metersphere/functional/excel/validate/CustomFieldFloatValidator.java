package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wx
 */
public class CustomFieldFloatValidator extends AbstractCustomFieldValidator {

    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        try {
            if (StringUtils.isNotBlank(value)) {
                Float.parseFloat(value);
            }
        } catch (Exception e) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_float_tip"), customField.getFieldName()));
        }
    }
}
