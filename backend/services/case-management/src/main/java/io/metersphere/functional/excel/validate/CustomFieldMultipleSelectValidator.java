package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wx
 */
public class CustomFieldMultipleSelectValidator extends CustomFieldSelectValidator {

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateArrayRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        prepareCache(customField);
        Set<String> idSet = optionValueSetCache.get(customField.getFieldId());
        Set<String> textSet = optionTextSetCache.get(customField.getFieldId());
        for (String item : parse2Array(customField.getFieldName(), value)) {
            if (!idSet.contains(item) && !textSet.contains(item)) {
                CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_select_tip"), customField.getFieldName(), textSet));
            }
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        if (StringUtils.isBlank(keyOrValuesStr)) {
            return JSON.toJSONString(new ArrayList<>());
        }
        List<String> keyOrValues = parse2Array(keyOrValuesStr);
        Map<String, String> nameMap = optionTextMapCache.get(customField.getFieldId());
        for (int i = 0; i < keyOrValues.size(); i++) {
            String item = keyOrValues.get(i);
            if (nameMap.containsKey(item)) {
                keyOrValues.set(i, nameMap.get(item));
            }
        }
        return JSON.toJSONString(keyOrValues);
    }

    @Override
    public Object parse2Value(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        if (StringUtils.isBlank(keyOrValuesStr) || StringUtils.equals(keyOrValuesStr, "[]")) {
            return StringUtils.EMPTY;
        }
        List list = JSON.parseArray(keyOrValuesStr);
        List<String> result = new ArrayList<>();
        Map<String, String> optionValueMap = customField.getOptions().stream().collect(Collectors.toMap(CustomFieldOption::getValue, CustomFieldOption::getText));
        list.forEach(item -> {
            if (optionValueMap.containsKey(item)) {
                result.add(optionValueMap.get(item));
            }
        });
        return String.join(",", JSON.parseArray(JSON.toJSONString(result)));
    }

}
