package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.track.dto.PlanReportIssueDTO;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtIssuesMapper {

    List<IssuesDao> getIssuesByCaseId(@Param("request") IssuesRequest issuesRequest);

    List<IssuesDao> getIssues(@Param("request") IssuesRequest issuesRequest);

    List<IssuesDao> getRelateIssues(@Param("request") IssuesRequest request);

    Issues getNextNum(String projectId);

    List<IssuesDao> getIssueForSync(String projectId);

    List<PlanReportIssueDTO> selectForPlanReport(String planId);

    List<IssuesDao>getCountByStatus(@Param("request") IssuesRequest issuesRequest);
}
