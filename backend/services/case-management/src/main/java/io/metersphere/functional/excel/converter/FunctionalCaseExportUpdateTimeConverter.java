package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.sdk.util.DateUtils;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportUpdateTimeConverter implements FunctionalCaseExportConverter {

    @Override
    public String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap) {
        return DateUtils.getTimeString(functionalCase.getUpdateTime());
    }
}
