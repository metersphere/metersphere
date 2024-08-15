package io.metersphere.functional.excel.converter;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.domain.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionalCaseExportCreateUserConverter implements FunctionalCaseExportConverter {

    public Map<String, String> userMap = new HashMap<>();

    public FunctionalCaseExportCreateUserConverter(String projectId) {
        ProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
        List<User> memberOption = projectApplicationService.getProjectUserList(projectId);
        memberOption.forEach(option -> userMap.put(option.getId(), option.getName()));
    }


    @Override
    public String parse(FunctionalCase functionalCase, Map<String, List<FunctionalCaseComment>> caseCommentMap, Map<String, List<TestPlanCaseExecuteHistory>> executeCommentMap, Map<String, List<CaseReviewHistory>> reviewCommentMap) {
        return getFromMapOfNullable(userMap, functionalCase.getCreateUser());
    }
}
