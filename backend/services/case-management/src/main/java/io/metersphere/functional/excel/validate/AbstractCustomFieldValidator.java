package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
public abstract class AbstractCustomFieldValidator {

    /**
     * 标记是否是键值对的选项
     * 需要校验时可以填键也可以填值
     */
    public Boolean isKVOption = false;

    /**
     * 校验参数是否合法
     *
     * @param customField
     * @param value
     */
    abstract public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException;


    /**
     * 将选项的值转化为对应的key
     *
     * @param keyOrValue
     * @return
     */
    public Object parse2Key(String keyOrValue, TemplateCustomFieldDTO customField) {
        return keyOrValue;
    }

    protected void validateRequired(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        if (customField.getRequired() && StringUtils.isBlank(value)) {
            if (StringUtils.equalsIgnoreCase(customField.getInternalFieldKey(),"functional_priority")) {
                return;
            }
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_required_tip"), customField.getFieldName()));
        }
    }

    protected void validateArrayRequired(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        if (customField.getRequired() && (StringUtils.isBlank(value) || StringUtils.equals(value, "[]"))) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_required_tip"), customField.getFieldName()));
        }
    }

    protected List<String> parse2Array(String name, String value) throws CustomFieldValidateException {
        try {
            //a,b,c => ["a","b","c"]
            return JSON.parseArray(JSON.toJSONString(value.split(",")));
        } catch (Exception e) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), name));
        }
        return new ArrayList<>();
    }

    protected List<String> parse2Array(String value) {
        try {
            return parse2Array(null, value);
        } catch (CustomFieldValidateException e) {
            LogUtils.error(e);
        }
        return new ArrayList<>();
    }

    public Object parse2Value(String value, TemplateCustomFieldDTO customField) {
        return value;
    }
}
