package io.metersphere.excel.utils;

import com.alibaba.excel.annotation.ExcelProperty;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * 表头国际化
 * 先调用 saveOriginalExcelProperty 存储原表头注解值
 * 再调用 translateExcelProperty 获取国际化的值
 * 最后调用 resetExcelProperty 重置为原来值，防止切换语言后无法国际化
 */
public class EasyExcelI18nTranslator {

    private Map<String, List<String>> excelPropertyMap = new HashMap<>();

    private Class clazz;

    public EasyExcelI18nTranslator(Class clazz) {
        this.clazz = clazz;
        saveOriginalExcelProperty();
    }

    private void readExcelProperty(Class clazz, BiConsumer<String, Map<String, Object>> operate) {
        Field field;
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                field = clazz.getDeclaredField(fields[i].getName());
                field.setAccessible(true);
                ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
                if (excelProperty != null) {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(excelProperty);
                    Field fieldValue = invocationHandler.getClass().getDeclaredField("memberValues");
                    fieldValue.setAccessible(true);
                    Map<String, Object> memberValues = null;
                    try {
                        memberValues = (Map<String, Object>) fieldValue.get(invocationHandler);
                    } catch (IllegalAccessException e) {
                        LogUtil.error(e.getMessage(), e);
                        throw new ExcelException(e.getMessage());
                    }

                    operate.accept(field.getName(), memberValues);

                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
        }


    }

    public void saveOriginalExcelProperty() {
        readExcelProperty(clazz, (fieldName, memberValues) -> {
            List<String> values = Arrays.asList((String[]) memberValues.get("value"));
            List<String> copyValues = new ArrayList<>();
            values.forEach(value -> {
                copyValues.add(value);
            });
            excelPropertyMap.put(fieldName, copyValues);
        });
    }

    public void translateExcelProperty() {
        readExcelProperty(TestCaseExcelData.class, (fieldName, memberValues) -> {
            String[] values = (String[]) memberValues.get("value");
            for (int j = 0; j < values.length; j++) {
                if (Pattern.matches("^\\{.+\\}$", values[j])) {
                    values[j] = Translator.get(values[j].substring(1, values[j].length() - 1));
                }
            }
            memberValues.put("value", values);
        });
    }

    public void resetExcelProperty() {
        readExcelProperty(clazz, (fieldName, memberValues) -> {
            String[] values = (String[]) memberValues.get("value");
            List<String> list = excelPropertyMap.get(fieldName);
            for (int j = 0; j < values.length; j++) {
                values[j] = list.get(j);
            }
            memberValues.put("value", values);
        });
    }
}
