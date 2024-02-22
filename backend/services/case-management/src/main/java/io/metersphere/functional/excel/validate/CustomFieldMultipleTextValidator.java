package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
public class CustomFieldMultipleTextValidator extends AbstractCustomFieldValidator {

    public CustomFieldMultipleTextValidator() {
        this.isKVOption = true;
    }

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isNotBlank(value)) {
            try {
                parse2Array(customField.getFieldName(), value);
            } catch (Exception e) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), customField.getFieldName()));
            }
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return JSON.toJSONString(new ArrayList<>());
        }
        List<String> keyOrValues = parse2Array(keyOrValuesStr);

        return JSON.toJSONString(keyOrValues);
    }
}
