package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.commons.vo.TaskResultVO;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtApiScenarioReportMapper {
    List<ApiScenarioReportResult> list(@Param("request") QueryAPIReportRequest request);

    ApiScenarioReportResult get(@Param("reportId") String reportId);

    long countByProjectID(String projectId);

    long countByProjectIdAndCreateInThisWeek(@Param("projectId") String projectId, @Param("executeType") String executeType, @Param("version") String version, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    long countByProjectIdAndCreateAndByScheduleInThisWeek(@Param("projectId") String projectId, @Param("executeType") String executeType, @Param("version") String version, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(@Param("projectId") String projectId, @Param("executeType") String executeType, @Param("version") String version);

    List<ApiScenarioReport> selectLastReportByIds(@Param("scenarioIdList") List<String> ids);

    ApiScenarioReport selectPreviousReportByScenarioId(@Param("scenarioId") String scenarioId, @Param("nowId") String nowId);

    List<String> idList(@Param("request") QueryAPIReportRequest request);

    List<ApiReportCountDTO> countByApiScenarioId();

    List<ApiScenarioReport> selectStatusByIds(@Param("ids") Collection<String> values);

    List<ApiScenarioReport> selectReportByProjectId(String projectId);

    List<PlanReportCaseDTO> selectForPlanReport(@Param("ids") List<String> reportIds);

    void update(@Param("ids") List<String> ids);

    @InsertProvider(type = ExtApiScenarioReportProvider.class, method = "insertListSql")
    void sqlInsert(List<ApiScenarioReportResult> list);

    List<TaskResultVO> findByProjectIds(@Param("request") TaskCenterRequest request);

    List<String> selectByProjectIdAndLessThanTime(@Param("projectId") String projectId, @Param("time") long time);

    void updateAllStatus();
}
