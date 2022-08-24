package io.metersphere.track.validate;

import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CustomFieldMultipleMemberValidator extends CustomFieldMemberValidator {

    @Override
    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateArrayRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }

        for (String item : parse2Array(customField.getName(), value)) {
            long count = userIdMap.entrySet().stream().filter(e -> StringUtils.equalsAnyIgnoreCase(item,e.getKey(),e.getValue())).count();
            if(count > 0){
                return;
            }
        }
        CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_member_tip"), customField.getName()));
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, CustomFieldDao customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return "";
        }
        List<String> keyOrValues = parse2Array(keyOrValuesStr);

        for (int i = 0; i < keyOrValues.size(); i++) {
            String item = keyOrValues.get(i).toLowerCase();
            if (userNameMap.containsKey(item)) {
                keyOrValues.set(i, userNameMap.get(item));
            }
        }
        return keyOrValues;
    }
}
