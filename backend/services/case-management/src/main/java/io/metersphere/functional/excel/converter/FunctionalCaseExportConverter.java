package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 功能用例导出时解析其他字段对应的列
 *
 * @author wx
 */
public interface FunctionalCaseExportConverter {

    String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap);

    default String getFromMapOfNullable(Map<String, String> map, String key) {
        if (StringUtils.isNotBlank(key)) {
            return map.get(key);
        }
        return StringUtils.EMPTY;
    }

    default String getFromMapOfNullableWithTranslate(Map<String, String> map, String key) {
        String value = getFromMapOfNullable(map, key);
        if (StringUtils.isNotBlank(value)) {
            return Translator.get(value);
        }
        return value;
    }
}
