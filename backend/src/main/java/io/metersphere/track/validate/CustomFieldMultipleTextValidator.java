package io.metersphere.track.validate;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldMultipleTextValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isNotBlank(value)) {
            try {
                JSONArray.parseArray(value);
            } catch (Exception e) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), customField.getName()));
            }
        }
    }
}
