package io.metersphere.sdk.util;

import org.apache.commons.lang3.StringUtils;

public class EnumValidator {
    /**
     * 校验枚举值
     *
     * @param enumClass 枚举类
     * @param value     枚举值
     * @param <E>       枚举类型
     * @return 枚举值
     */
    public static <E extends Enum<E>> E validateEnum(Class<E> enumClass, String value) {
        if (StringUtils.isBlank(value)) {
            LogUtils.error("Invalid value for enum " + enumClass.getSimpleName() + ": " + value);
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            LogUtils.error("Invalid value for enum " + enumClass.getSimpleName() + ": " + value, e);
            return null;
        }
    }
}
