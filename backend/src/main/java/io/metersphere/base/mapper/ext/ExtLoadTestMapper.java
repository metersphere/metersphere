package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.performance.request.QueryTestPlanRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtLoadTestMapper {
    List<LoadTestDTO> list(@Param("request") QueryTestPlanRequest params);

    List<LoadTest> getLoadTestByProjectId(String projectId);

    int checkLoadTestOwner(@Param("testId") String testId, @Param("workspaceIds") Set<String> workspaceIds);

    LoadTest getNextNum(@Param("projectId") String projectId);

    List<FileMetadata> getProjectFiles(@Param("projectId") String projectId, @Param("loadTypes") List<String> loadType);

}
