package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportUpdateUserConverter extends FunctionalCaseExportCreateUserConverter {


    public FunctionalCaseExportUpdateUserConverter(String projectId) {
        super(projectId);
    }

    @Override
    public String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap) {
        return getFromMapOfNullable(userMap, functionalCase.getUpdateUser());
    }
}
