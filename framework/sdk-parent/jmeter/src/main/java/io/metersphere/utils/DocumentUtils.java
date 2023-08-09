package io.metersphere.utils;

import com.google.gson.GsonBuilder;
import io.metersphere.vo.Condition;
import io.metersphere.vo.ElementCondition;
import net.minidev.json.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class DocumentUtils {

    public static boolean documentChecked(Object subj, String condition, ThreadLocal<DecimalFormat> decimalFormatter) {
        if (StringUtils.isNotEmpty(condition)) {
            ElementCondition elementCondition = JsonUtils.parseObject(condition, ElementCondition.class);
            boolean isTrue = true;
            if (CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
                for (Condition item : elementCondition.getConditions()) {
                    String expectedValue = item.getValue() != null ? item.getValue().toString() : "";
                    String resValue = objectToString(subj, decimalFormatter);
                    switch (item.getKey()) {
                        case "value_eq":
                            isTrue = valueEquals(resValue, expectedValue);
                            break;
                        case "value_not_eq":
                            isTrue = valueNotEquals(resValue, expectedValue);
                            break;
                        case "value_in":
                            isTrue = StringUtils.contains(resValue, expectedValue);
                            break;
                        case "length_eq":
                            isTrue = getLength(resValue, decimalFormatter) == numberOf(item.getValue());
                            break;
                        case "length_not_eq":
                            isTrue = getLength(subj, decimalFormatter) != numberOf(item.getValue());
                            break;
                        case "length_gt":
                            isTrue = getLength(subj, decimalFormatter) > numberOf(item.getValue());
                            break;
                        case "length_lt":
                            isTrue = getLength(subj, decimalFormatter) < numberOf(item.getValue());
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
            return isTrue && checkType(elementCondition, subj);
        }
        return true;
    }

    public static boolean checkType(ElementCondition elementCondition, Object subj) {
        if (elementCondition.isTypeVerification()) {
            return StringUtils.equalsIgnoreCase(elementCondition.getType(), "object") || (subj != null
                    && StringUtils.equalsIgnoreCase(elementCondition.getType(), getType(subj)));
        }
        return true;
    }

    public static String getType(Object object) {
        String type = object.getClass().getName().substring(object.getClass().getName().lastIndexOf(".") + 1);
        if (StringUtils.equalsIgnoreCase("Integer", type)) {
            return type.toLowerCase();
        } else if (StringUtils.equalsAnyIgnoreCase(type, "integer", "float", "long", "double", "short", "byte", "bigdecimal", "biginteger")) {
            return "number";
        } else if (StringUtils.indexOfAny(type, "Array", "List") != -1) {
            return "array";
        }
        return type.toLowerCase();
    }

    private static boolean valueEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createNumber(v1);
            Number number2 = NumberUtils.createNumber(v2);
            return number1.equals(number2);
        } catch (Exception e) {
            return StringUtils.equals(v1, v2);
        }
    }

    private static boolean valueNotEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createNumber(v1);
            Number number2 = NumberUtils.createNumber(v2);
            return !number1.equals(number2);
        } catch (Exception e) {
            return !StringUtils.equals(v1, v2);
        }
    }

    public static String objectToString(Object subj, ThreadLocal<DecimalFormat> decimalFormatter) {
        String str;
        if (subj == null) {
            str = "null";
        } else if (subj instanceof Map) {
            str = new GsonBuilder().serializeNulls().create().toJson(subj);
        } else if (!(subj instanceof Double) && !(subj instanceof Float)) {
            str = subj.toString();
        } else {
            str = ((DecimalFormat) decimalFormatter.get()).format(subj);
        }
        return str;
    }


    private static int getLength(Object value, ThreadLocal<DecimalFormat> decimalFormatter) {
        if (value != null) {
            String resValue = objectToString(value, decimalFormatter);
            if (StringUtils.equals(resValue, "[[]]")) {
                return 0;
            }
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

    private static long numberOf(Object value) {
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private static String arrayMatched(JSONArray value) {
        if (value.isEmpty()) {
            return "array";
        } else {
            Object[] var2 = value.toArray();
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object subj = var2[var4];
                return getType(subj);
            }
        }
        return getType(value);
    }

    public static String documentMsg(String name, Object resValue, String condition, ThreadLocal<DecimalFormat> decimalFormatter) {
        String msg = "";
        if (StringUtils.isNotEmpty(condition)) {
            ElementCondition elementCondition = JsonUtils.parseObject(condition, ElementCondition.class);
            if (CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
                for (Condition item : elementCondition.getConditions()) {
                    if (StringUtils.equalsAny(item.getKey(), "value_eq", "value_not_eq", "value_in")) {
                        msg = resValue != null ? resValue.toString() : "";
                    } else if (StringUtils.equalsAny(item.getKey(), "length_eq", "length_not_eq", "length_gt", "length_lt")) {
                        msg = "长度是：" + getLength(resValue, decimalFormatter) + "";
                    } else {
                        msg = resValue != null ? resValue.toString() : "";
                    }
                }
            }
            if (!checkType(elementCondition, resValue)) {
                if (resValue instanceof JSONArray) {
                    msg = " 类型：" + (arrayMatched((JSONArray) resValue)) + ", 值：" + msg;
                } else {
                    msg = " 类型：" + getType(resValue) + ", 值：" + msg;
                }
            }
        }
        return (StringUtils.isNotEmpty(name) ? name.split("==")[1] : "") + "校验失败，实际返回：" + msg;
    }
}
