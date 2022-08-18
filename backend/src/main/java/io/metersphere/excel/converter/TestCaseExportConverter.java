package io.metersphere.excel.converter;

import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 功能用例导出时解析其他字段对应的列
 * @author jianxing
 */
public interface TestCaseExportConverter {

    String parse(TestCaseDTO testCase);

    default String getFromMapOfNullable(Map<String, String> map, String key) {
        if (StringUtils.isNotBlank(key)) {
            return map.get(key);
        }
        return "";
    }

    default String getFromMapOfNullableWithTranslate(Map<String, String> map, String key) {
        String value = getFromMapOfNullable(map, key);
        if (StringUtils.isNotBlank(value)) {
            return Translator.get(value);
        }
        return value;
    }
}
