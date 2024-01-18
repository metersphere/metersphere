package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author wx
 */
public class CustomFieldMultipleMemberValidator extends CustomFieldMemberValidator {

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateArrayRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }

        for (String item : parse2Array(customField.getFieldName(), value)) {
            item = item.toLowerCase();
            if (!userIdMap.containsKey(item) && !userNameMap.containsKey(item)) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_member_tip"), customField.getFieldName()));
            }
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return StringUtils.EMPTY;
        }
        List<String> keyOrValues = parse2Array(keyOrValuesStr);

        for (int i = 0; i < keyOrValues.size(); i++) {
            String item = keyOrValues.get(i).toLowerCase();
            if (userIdMap.containsKey(item)) {
                keyOrValues.set(i, userIdMap.get(item));
            }
            if (userNameMap.containsKey(item)) {
                keyOrValues.set(i, userNameMap.get(item));
            }
        }
        return keyOrValues;
    }
}
