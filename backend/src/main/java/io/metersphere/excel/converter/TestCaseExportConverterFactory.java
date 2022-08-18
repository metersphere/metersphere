package io.metersphere.excel.converter;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.constants.TestCaseExportOtherField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseExportConverterFactory {

    public static Map<String, TestCaseExportConverter> getConverters(List<String> keys) {
        Map<String, TestCaseExportConverter> converterMapResult = new HashMap<>();
        try {
            HashMap<String, Class<? extends TestCaseExportConverter>> converterMap = getConverterMap();
            for (String key : keys) {
                Class<? extends TestCaseExportConverter> clazz = converterMap.get(key);
                if (clazz != null) {
                    converterMapResult.put(key, clazz.getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return converterMapResult;
    }

    public static TestCaseExportConverter getConverter(String key) {
        try {
            Class<? extends TestCaseExportConverter> clazz = getConverterMap().get(key);
            if (clazz != null) {
               return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    private static HashMap<String, Class<? extends TestCaseExportConverter>> getConverterMap() {
        return new HashMap<>() {{
           put(TestCaseExportOtherField.VERSION.getValue(), TestCaseExportVersionConverter.class);
           put(TestCaseExportOtherField.COMMEND.getValue(), TestCaseExportCommendConverter.class);
           put(TestCaseExportOtherField.EXECUTE_RESULT.getValue(), TestCaseExportExecuteResultConverter.class);
           put(TestCaseExportOtherField.REVIEW_RESULT.getValue(), TestCaseExportReviewResultConverter.class);
           put(TestCaseExportOtherField.CREATOR.getValue(), TestCaseExportCreatorConverter.class);
           put(TestCaseExportOtherField.CREATE_TIME.getValue(), TestCaseExportCreateTimeConverter.class);
           put(TestCaseExportOtherField.UPDATE_TIME.getValue(), TestCaseExportUpdateTimeConverter.class);
        }};
    }
}
