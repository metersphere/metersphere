package io.metersphere.functional.excel.converter;

import io.metersphere.functional.excel.constants.FunctionalCaseExportOtherField;
import io.metersphere.sdk.util.LogUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportConverterFactory {

    public static Map<String, FunctionalCaseExportConverter> getConverters(List<String> keys, String projectId) {
        Map<String, FunctionalCaseExportConverter> converterMapResult = new HashMap<>();
        try {
            HashMap<String, Class<? extends FunctionalCaseExportConverter>> converterMap = getConverterMap(projectId);
            for (String key : keys) {
                Class<? extends FunctionalCaseExportConverter> clazz = converterMap.get(key);
                if (clazz != null) {
                    // 获取所有构造函数
                    Constructor<?>[] constructors = clazz.getConstructors();
                    // 遍历构造函数，选择适当的构造函数进行实例化
                    for (Constructor<?> constructor : constructors) {
                        Class<?>[] parameterTypes = constructor.getParameterTypes();
                        if (parameterTypes.length == 0) {
                            // 如果是无参构造，直接调用 newInstance
                            converterMapResult.put(key, clazz.getDeclaredConstructor().newInstance());
                            break;
                        } else {
                            // 如果是有参构造，可以传递参数
                            converterMapResult.put(key, clazz.getDeclaredConstructor(String.class).newInstance(projectId));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return converterMapResult;
    }

    public static FunctionalCaseExportConverter getConverter(String key, String projectId) {
        try {
            Class<? extends FunctionalCaseExportConverter> clazz = getConverterMap(projectId).get(key);
            if (clazz != null) {
                return clazz.getDeclaredConstructor(String.class).newInstance(projectId);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }

    private static HashMap<String, Class<? extends FunctionalCaseExportConverter>> getConverterMap(String projectId) {
        return new HashMap<>() {{
            put(FunctionalCaseExportOtherField.CREATE_USER.getValue(), FunctionalCaseExportCreateUserConverter.class);
            put(FunctionalCaseExportOtherField.CREATE_TIME.getValue(), FunctionalCaseExportCreateTimeConverter.class);
            put(FunctionalCaseExportOtherField.UPDATE_USER.getValue(), FunctionalCaseExportUpdateUserConverter.class);
            put(FunctionalCaseExportOtherField.UPDATE_TIME.getValue(), FunctionalCaseExportUpdateTimeConverter.class);
            put(FunctionalCaseExportOtherField.REVIEW_STATUS.getValue(), FunctionalCaseExportReviewStatusConverter.class);
            put(FunctionalCaseExportOtherField.LAST_EXECUTE_RESULT.getValue(), FunctionalCaseExportExecuteStatusConverter.class);
            put(FunctionalCaseExportOtherField.CASE_COMMENT.getValue(), FunctionalCaseExportCaseCommentConverter.class);
            put(FunctionalCaseExportOtherField.EXECUTE_COMMENT.getValue(), FunctionalCaseExportExecuteCommentConverter.class);
            put(FunctionalCaseExportOtherField.REVIEW_COMMENT.getValue(), FunctionalCaseExportReviewCommentConverter.class);
        }};
    }
}
