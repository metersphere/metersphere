package io.metersphere.track.validate;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomFieldMultipleSelectValidator extends CustomFieldSelectValidator {

    @Override
    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        Set<String> idSet = optionValueSetCache.get(customField.getId());
        Set<String> textSet = optionTextSetCache.get(customField.getId());
        for (String item : parse2Array(customField.getName(), value)) {
            if (!idSet.contains(item) && !textSet.contains(item)) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_select_tip"), customField.getName(), textSet));
            }
        }
    }

    @Override
    public String parse2Key(String keyOrValuesStr, CustomFieldDao customField) {
        List<String> keyOrValues = JSONArray.parseArray(keyOrValuesStr, String.class);
        Map<String, String> nameMap = optionTextMapCache.get(customField.getId());
        for (int i = 0; i < keyOrValues.size(); i++) {
            String item = keyOrValues.get(i);
            if (nameMap.containsKey(item)) {
                keyOrValues.set(i, nameMap.get(item));
            }
        }
        return JSONArray.toJSONString(keyOrValues);
    }
}
