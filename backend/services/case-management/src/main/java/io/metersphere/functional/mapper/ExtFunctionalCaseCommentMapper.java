package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFunctionalCaseCommentMapper {
    List<FunctionalCaseComment> getCaseComment(@Param("ids") List<String> ids);

    List<TestPlanCaseExecuteHistory> getExecuteComment(@Param("ids") List<String> ids);

    List<CaseReviewHistory> getReviewComment(@Param("ids") List<String> ids);
}
