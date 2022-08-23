package io.metersphere.track.validate;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    abstract public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException;

    /**
     * 将选项的值转化为对应的key
     *
     * @param keyOrValue
     * @return
     */
    public Object parse2Key(String keyOrValue, CustomFieldDao customField) {
        return keyOrValue;
    }

    protected void validateRequired(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        if (customField.getRequired() && StringUtils.isBlank(value)) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_required_tip"), customField.getName()));
        }
    }

    protected void validateArrayRequired(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        if (customField.getRequired() && (StringUtils.isBlank(value) || StringUtils.equals(value, "[]"))) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_required_tip"), customField.getName()));
        }
    }

    protected List<String> parse2Array(String name, String value) throws CustomFieldValidateException {
        try {
            // [a, b] => ["a","b"]
            if (!StringUtils.equals(value, "[]")) {
                value = value.replace("[", "[\"")
                        .replace("]", "\"]")
                        .replace(",", "\",\"")
                        .replace("，", "\"，\"")
                        .replace(" ", "");
            }
            return JSONArray.parseArray(value, String.class);
        } catch (Exception e) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_array_tip"), name));
        }
        return new ArrayList<>();
    }

    protected List<String> parse2Array(String value) {
        try {
            return parse2Array(null, value);
        } catch (CustomFieldValidateException e) {
            LogUtil.error(e);
        }
        return new ArrayList<>();
    }
}
