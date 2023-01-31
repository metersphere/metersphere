package io.metersphere.api.exec.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatterUtil {
    public static boolean isNumber(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return false;
        }
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
        Matcher isNum = pattern.matcher(obj.toString());
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static JsonElement getElementValue(JsonObject object) {
        return object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK);
    }

    public static String getMockValue(JsonObject object) {
        return ScriptEngineUtils.buildFunctionCallString(getStrValue(object));
    }

    public static String getStrValue(JsonObject object) {
        return getElementValue(object).getAsString();
    }

    public static boolean isMockValue(JsonObject object) {
        return object.has(PropertyConstant.MOCK)
                && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                && FormatterUtil.getElementValue(object) != null
                && StringUtils.isNotEmpty(FormatterUtil.getElementValue(object).getAsString());
    }

}
