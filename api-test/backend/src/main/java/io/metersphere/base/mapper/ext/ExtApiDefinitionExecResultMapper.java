package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExpand;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.commons.vo.TaskResultVO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtApiDefinitionExecResultMapper {

    List<ApiDefinitionExecResultExpand> list(@Param("request") QueryAPIReportRequest request);

    void deleteByResourceId(String id);

    ApiDefinitionExecResultWithBLOBs selectMaxResultByResourceId(String resourceId);

    String selectMaxResultIdByResourceId(String resourceId);

    ApiDefinitionExecResultWithBLOBs selectMaxResultByResourceIdAndType(String resourceId, String type);

    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("version") String version, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    String selectExecResult(String resourceId);

    ApiDefinitionExecResultWithBLOBs selectPlanApiMaxResultByTestIdAndType(String resourceId, String type);

    List<ApiDefinitionExecResult> selectStatusByIdList(@Param("ids") Collection<String> values);

    void update(@Param("ids") List<String> ids);

    @InsertProvider(type = ExtApiDefinitionExecResultProvider.class, method = "insertListSql")
    void sqlInsert(List<ApiDefinitionExecResult> list);

    List<TaskResultVO> findByProjectIds(@Param("request") TaskCenterRequest request);

    List<String> selectDistinctStatusByReportId(String reportId);

    String selectResourceId(String id);

    List<ApiDefinitionExecResultWithBLOBs> selectRerunResult(@Param("reportId") String reportId);

    List<String> selectByProjectIdAndLessThanTime(@Param("projectId") String projectId, @Param("time") long time);

    List<ApiDefinitionExecResultWithBLOBs> selectByResourceIdsAndMaxCreateTime(@Param("ids") List<String> resourceIds);

    void updateAllStatus();

    List<PlanReportCaseDTO> selectForPlanReport(@Param("ids") List<String> apiReportIds);
}
