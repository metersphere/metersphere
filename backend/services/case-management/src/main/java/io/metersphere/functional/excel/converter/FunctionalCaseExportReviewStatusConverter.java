package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportReviewStatusConverter implements FunctionalCaseExportConverter {

    private Map<String, String> caseReviewStatusMap = new HashMap<>();

    public FunctionalCaseExportReviewStatusConverter() {
        for (FunctionalCaseReviewStatus value : FunctionalCaseReviewStatus.values()) {
            caseReviewStatusMap.put(value.name(), value.getI18nKey());
        }
    }

    @Override
    public String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap) {
        return getFromMapOfNullableWithTranslate(caseReviewStatusMap, functionalCase.getReviewStatus());
    }
}
