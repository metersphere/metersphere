package org.apache.jmeter.assertions;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import io.metersphere.api.dto.definition.request.assertions.document.Condition;
import io.metersphere.api.dto.definition.request.assertions.document.ElementCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class DocumentUtils {

    public static boolean documentChecked(Object subj, String condition, ThreadLocal<DecimalFormat> decimalFormatter) {
        if (StringUtils.isNotEmpty(condition)) {
            ElementCondition elementCondition = JSON.parseObject(condition, ElementCondition.class);
            boolean isTrue = true;
            if (CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
                for (Condition item : elementCondition.getConditions()) {
                    String expectedValue = ifValue(item.getValue());
                    String resValue = objectToString(subj, decimalFormatter);
                    switch (item.getKey()) {
                        case "value_eq":
                            isTrue = StringUtils.equals(resValue, expectedValue);
                            break;
                        case "value_not_eq":
                            isTrue = !StringUtils.equals(resValue, expectedValue);
                            break;
                        case "value_in":
                            isTrue = StringUtils.contains(resValue, expectedValue);
                            break;
                        case "length_eq":
                            isTrue = getLength(subj, decimalFormatter) == getLength(item.getValue(), decimalFormatter);
                            break;
                        case "length_not_eq":
                            isTrue = getLength(subj, decimalFormatter) != getLength(item.getValue(), decimalFormatter);
                            break;
                        case "length_gt":
                            isTrue = getLength(subj, decimalFormatter) > getLength(item.getValue(), decimalFormatter);
                            break;
                        case "length_lt":
                            isTrue = getLength(subj, decimalFormatter) < getLength(item.getValue(), decimalFormatter);
                            break;
                        case "regular":
                            Pattern pattern = JMeterUtils.getPatternCache().getPattern(expectedValue);
                            isTrue = JMeterUtils.getMatcher().matches(resValue, pattern);
                            break;
                    }
                    if (!isTrue) {
                        break;
                    }
                }
            }
            return isTrue;
        }
        return true;
    }

    public static String objectToString(Object subj, ThreadLocal<DecimalFormat> decimalFormatter) {
        String str;
        if (subj == null) {
            str = "null";
        } else if (subj instanceof Map) {
            str = new Gson().toJson(subj);
        } else if (!(subj instanceof Double) && !(subj instanceof Float)) {
            str = subj.toString();
        } else {
            str = ((DecimalFormat) decimalFormatter.get()).format(subj);
        }

        return str;
    }


    private static int getLength(Object value) {
        if (value != null) {
            if (value instanceof List) {
                return ((List) value).size();
            }
            return value.toString().length();
        }
        return 0;
    }

    private static String ifValue(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }

    private static int getLength(Object value, ThreadLocal<DecimalFormat> decimalFormatter) {
        if (value != null) {
            if (value instanceof Map) {
                return ((Map) value).size();
            } else if (value instanceof List) {
                return ((List) value).size();
            } else if (!(value instanceof Double) && !(value instanceof Float)) {
                return value.toString().length();
            } else {
                return ((DecimalFormat) decimalFormatter.get()).format(value).length();
            }
        }
        return 0;
    }


    public static String documentMsg(Object resValue, String condition) {
        String msg = "";
        if (StringUtils.isNotEmpty(condition)) {
            ElementCondition elementCondition = JSON.parseObject(condition, ElementCondition.class);
            if (CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
                for (Condition item : elementCondition.getConditions()) {
                    if (StringUtils.equalsAny(item.getKey(), "value_eq", "value_not_eq", "value_in")) {
                        msg = resValue != null ? resValue.toString() : "";
                    } else if (StringUtils.equalsAny(item.getKey(), "length_eq", "length_not_eq", "length_gt", "length_lt")) {
                        msg = "长度是：" + getLength(resValue) + "";
                    } else {
                        msg = resValue != null ? resValue.toString() : "";
                    }
                }
            }
        }
        return msg;
    }

}
