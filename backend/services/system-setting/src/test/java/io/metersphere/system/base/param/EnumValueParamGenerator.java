package io.metersphere.system.base.param;


import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.valid.EnumValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianxing
 */
public class EnumValueParamGenerator extends ParamGenerator {

    /**
     * 生成一个不在枚举值中的数值
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        EnumValue enumValue = (EnumValue) annotation;
        Class<? extends Enum> enumClass = enumValue.enumClass();
        String[] excludeValues = enumValue.excludeValues();
        // 获取枚举的所有实例
        Enum<? extends Enum>[] enums = enumClass.getEnumConstants();
        boolean isStr = true;
        if (enums[0] instanceof ValueEnum) {
            Object value = ((ValueEnum<?>) enums[0]).getValue();
            // 获取枚举值，判断是否是字符串类型
            if (value != null && !(value instanceof String)) {
                isStr = false;
            }
        }
        // 如果有排除值，则返回排除值
        if (excludeValues != null && excludeValues.length > 0) {
            return excludeValues[0];
        }
        // 这里伪生成一个不在枚举值中的数值，仅支持字符串和整型
        return isStr ? "1" : Integer.MAX_VALUE;
    }
}
