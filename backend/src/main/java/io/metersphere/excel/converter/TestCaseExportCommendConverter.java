package io.metersphere.excel.converter;

import io.metersphere.commons.constants.TestCaseCommentType;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.constants.TestReviewCaseStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseCommentDTO;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.service.TestCaseCommentService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

public class TestCaseExportCommendConverter implements TestCaseExportConverter {

    private HashMap<String, String> commendTypeMap = new HashMap<>();
    private HashMap<String, String> planCaseStatusMap = new HashMap<>();
    private HashMap<String, String> reviewCaseStatusMap = new HashMap<>();

    public TestCaseExportCommendConverter() {
        commendTypeMap.put(TestCaseCommentType.CASE.name(), "test_case_comment");
        commendTypeMap.put(TestCaseCommentType.PLAN.name(), "test_case_plan_comment");
        commendTypeMap.put(TestCaseCommentType.REVIEW.name(), "test_case_review_comment");

        planCaseStatusMap.put(TestPlanTestCaseStatus.Pass.name(), "execute_pass");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Underway.name(), "test_case_status_prepare");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Blocking.name(), "plan_case_status_blocking");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Failure.name(), "test_case_status_error");
        planCaseStatusMap.put(TestPlanTestCaseStatus.Skip.name(), "plan_case_status_skip");

        reviewCaseStatusMap.put(TestReviewCaseStatus.Prepare.name(), "test_case_status_prepare");
        reviewCaseStatusMap.put(TestReviewCaseStatus.Pass.name(), "execute_pass");
        reviewCaseStatusMap.put(TestReviewCaseStatus.UnPass.name(), "execute_not_pass");
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        TestCaseCommentService testCaseCommentService = CommonBeanFactory.getBean(TestCaseCommentService.class);
        List<TestCaseCommentDTO> caseComments = testCaseCommentService.getCaseComments(testCase.getId());
        StringBuilder result = new StringBuilder();
        String template = Translator.get("test_case_comment_template");
        caseComments.forEach(comment -> {
            String authorName = comment.getAuthorName();
            String type = getFromMapOfNullableWithTranslate(commendTypeMap, comment.getType());
            String status = "";
            if (StringUtils.equals(comment.getType(), TestCaseCommentType.PLAN.name())) {
                status = getFromMapOfNullableWithTranslate(planCaseStatusMap, comment.getStatus());
                status = "[".concat(status).concat("]");
            } else if (StringUtils.equals(comment.getType(), TestCaseCommentType.REVIEW.name())) {
                status = getFromMapOfNullableWithTranslate(reviewCaseStatusMap, comment.getStatus());
                status = "[".concat(status).concat("]");
            }
            String updateTime = DateUtils.getTimeString(comment.getUpdateTime());
            String description = comment.getDescription();
            result.append(String.format(template, authorName, type, status, updateTime, description));
        });
        return result.toString();
    }
}
