package io.metersphere.functional.excel.converter;

import io.metersphere.functional.excel.constants.FunctionalCaseExportOtherField;
import io.metersphere.sdk.util.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportConverterFactory {

    public static Map<String, FunctionalCaseExportConverter> getConverters(List<String> keys) {
        Map<String, FunctionalCaseExportConverter> converterMapResult = new HashMap<>();
        try {
            HashMap<String, Class<? extends FunctionalCaseExportConverter>> converterMap = getConverterMap();
            for (String key : keys) {
                Class<? extends FunctionalCaseExportConverter> clazz = converterMap.get(key);
                if (clazz != null) {
                    converterMapResult.put(key, clazz.getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return converterMapResult;
    }

    public static FunctionalCaseExportConverter getConverter(String key) {
        try {
            Class<? extends FunctionalCaseExportConverter> clazz = getConverterMap().get(key);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }

    private static HashMap<String, Class<? extends FunctionalCaseExportConverter>> getConverterMap() {
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
