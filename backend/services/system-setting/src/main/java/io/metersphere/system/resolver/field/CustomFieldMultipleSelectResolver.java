package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.CustomFieldDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomFieldMultipleSelectResolver extends CustomFieldSelectResolver {

    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateArrayRequired(customField, value);
        validateArray(customField.getName(), value);
        List<CustomFieldOption> options = getOptions(customField.getId());
        Set<String> values = options.stream().map(CustomFieldOption::getValue).collect(Collectors.toSet());
        for (String item : (List<String>)value) {
            if (!values.contains(item)) {
                throwValidateException(customField.getName());
            }
        }
    }

    @Override
    public String parse2String(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public Object parse2Value(String value) {
        return parse2Array(value);
    }
}
