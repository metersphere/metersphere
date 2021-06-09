package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionExecResultMapper {

    void deleteByResourceId(String id);

    ApiDefinitionExecResult selectMaxResultByResourceId(String resourceId);

    ApiDefinitionExecResult selectMaxResultByResourceIdAndType(String resourceId, String type);


    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    long countByTestCaseIDInProject(String projectId);

    List<ExecutedCaseInfoResult> findFaliureCaseInfoByProjectIDAndExecuteTimeAndLimitNumber(@Param("projectId") String projectId, @Param("startTimestamp") long startTimestamp);

    String  selectExecResult(String resourceId);
}