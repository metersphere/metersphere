package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportExecuteCommentConverter implements FunctionalCaseExportConverter {

    private Map<String, String> executeStatusMap = new HashMap<>();

    public FunctionalCaseExportExecuteCommentConverter() {
        for (FunctionalCaseExecuteStatus value : FunctionalCaseExecuteStatus.values()) {
            executeStatusMap.put(value.name(), value.getI18nKey());
        }
    }


    @Override
    public String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap) {
        if (executeCommentMap.containsKey(functionalCase.getId())) {
            StringBuilder result = new StringBuilder();
            String template = Translator.get("functional_case_execute_comment_template");
            List<TestPlanCaseExecuteHistory> executeComment = executeCommentMap.get(functionalCase.getId());
            executeComment.forEach(item -> {
                String status = getFromMapOfNullableWithTranslate(executeStatusMap, item.getStatus());
                String createTime = DateUtils.getTimeString(item.getCreateTime());
                String content = parseHtml(new String(item.getContent() == null ? new byte[0] : item.getContent(), StandardCharsets.UTF_8));
                result.append(String.format(template, item.getCreateUser(), status, createTime, content));
            });
            return result.toString();
        }
        return StringUtils.EMPTY;
    }
}
