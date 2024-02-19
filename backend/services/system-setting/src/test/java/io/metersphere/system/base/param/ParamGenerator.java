package io.metersphere.system.base.param;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 单元测试参数生成器
 * @author jianxing
 */
public abstract class ParamGenerator {

    /**
     * 生成非法参数
     * @param annotation 注解
     * @param field 字段
     * @return 非法参数
     */
    public abstract Object invalidGenerate(Annotation annotation, Field field);

    /**
     * 通过反射获取注解上的分组信息，判断是否匹配
     * @param group 当前分组
     * @param annotation 注解
     * @return 是否匹配
     */
    public boolean isGroupMatch(Class group, Annotation annotation) {
        Method[] methods = annotation.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (StringUtils.equals(method.getName(), "groups")) {
                try {
                    Class[] groups = (Class[]) method.invoke(annotation);
                    if (groups.length == 0) {
                        // 注解上没有分组，如果当前分组为空，则匹配，否则不匹配
                        return group == null;
                    }
                    for (Class groupItem : groups) {
                        if (groupItem.equals(group)) {
                            // 有一个匹配成功则返回true
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    protected boolean isNumberType(Class<?> clazz) {
        return (clazz.isPrimitive() && clazz != boolean.class && clazz != char.class)
                || Number.class.isAssignableFrom(clazz);
    }

    protected <T> T convertToNumberType(Class<T> clazz, long value) {
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf((int) value);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == short.class || clazz == Short.class) {
            return (T) Short.valueOf((short) value);
        } else if (clazz == byte.class || clazz == Byte.class) {
            return (T) Byte.valueOf((byte) value);
        } else if (clazz == float.class || clazz == Float.class) {
            return (T) Float.valueOf((float) value);
        } else if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf((double) value);
        } else {
            throw new IllegalArgumentException("不支持的类型：" + clazz);
        }
    }
}
