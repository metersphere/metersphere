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
        for (TestCaseCommentType value : TestCaseCommentType.values()) {
            commendTypeMap.put(value.name(), value.getI18nKey());
        }

        for (TestPlanTestCaseStatus value : TestPlanTestCaseStatus.values()) {
            planCaseStatusMap.put(value.name(), value.getI18nKey());
        }

        for (TestReviewCaseStatus value : TestReviewCaseStatus.values()) {
            reviewCaseStatusMap.put(value.name(), value.getI18nKey());
        }
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
