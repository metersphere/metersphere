package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.performance.request.QueryProjectFileRequest;
import io.metersphere.performance.request.QueryTestPlanRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtLoadTestMapper {
    List<LoadTestDTO> list(@Param("request") QueryTestPlanRequest params);

    List<LoadTest> getLoadTestByProjectId(String projectId);

    int checkLoadTestOwner(@Param("testId") String testId, @Param("projectIds") Set<String> projectIds);

    LoadTest getNextNum(@Param("projectId") String projectId);

    List<FileMetadata> getProjectFiles(@Param("projectId") String projectId, @Param("loadTypes") List<String> loadType,
                                       @Param("request") QueryProjectFileRequest request);

    List<String> selectProjectIds();

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId")String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId")String projectId, @Param("baseOrder") Long baseOrder);

    int moduleCount(@Param("request") QueryTestPlanRequest request);
}
