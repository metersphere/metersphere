package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.CustomFieldDao;

public class CustomFieldMultipleMemberResolver extends CustomFieldMemberResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateArrayRequired(customField, value);
        validateArray(customField.getName(), value);
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
