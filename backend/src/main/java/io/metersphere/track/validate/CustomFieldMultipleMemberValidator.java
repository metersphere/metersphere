package io.metersphere.track.validate;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CustomFieldMultipleMemberValidator extends CustomFieldMemberValidator {

    @Override
    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }

        for (String item : parse2Array(customField.getName(), value)) {
            if (!userIdMap.containsKey(item) && !userNameMap.containsKey(item)) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_member_tip"), customField.getName()));
            }
        }
    }

    @Override
    public String parse2Key(String keyOrValuesStr, CustomFieldDao customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return "";
        }
        List<String> keyOrValues = JSONArray.parseArray(keyOrValuesStr, String.class);

        for (int i = 0; i < keyOrValues.size(); i++) {
            String item = keyOrValues.get(i);
            if (userNameMap.containsKey(item)) {
                keyOrValues.set(i, userNameMap.get(item));
            }
        }
        return JSONArray.toJSONString(keyOrValues);
    }
}
