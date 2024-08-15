package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wx
 */
public class CustomFieldMultipleTextValidator extends AbstractCustomFieldValidator {
    protected static final int MULTIP_INPUT_COUNT = 15;
    protected static final int MULTIP_INPUT_LENGTH = 64;

    public CustomFieldMultipleTextValidator() {
        this.isKVOption = true;
    }

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isNotBlank(value)) {
            List<String> multipTexts = parse2Array(customField.getFieldName(), value);
            if (multipTexts.size() > MULTIP_INPUT_COUNT) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_multip_input_tip"), customField.getFieldName()));
            }
            AtomicBoolean isOverLength = new AtomicBoolean(false);
            multipTexts.forEach(multipText -> {
                if (multipText.length() > MULTIP_INPUT_LENGTH) {
                    isOverLength.set(true);
                }
            });
            if (isOverLength.get()) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_multip_input_length_tip"), customField.getFieldName()));
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

    @Override
    public Object parse2Value(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        if (StringUtils.isBlank(keyOrValuesStr) || StringUtils.equals(keyOrValuesStr, "[]")) {
            return JSON.toJSONString(new ArrayList<>());
        }
        String keyOrValues = String.join(",", JSON.parseArray(keyOrValuesStr));
        return keyOrValues;
    }
}
