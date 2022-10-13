package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.LoadCaseCountChartResult;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.LoadCaseCountRequest;
import io.metersphere.request.QueryProjectFileRequest;
import io.metersphere.request.QueryTestPlanRequest;
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

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    int moduleCount(@Param("request") QueryTestPlanRequest request);

    void addLatestVersion(String refId);

    void clearLatestVersion(String refId);

    List<String> selectRefIdsForVersionChange(@Param("versionId") String versionId, @Param("projectId") String projectId);

    List<FileMetadata> getFileMetadataByIds(@Param("testId") String testId);

    List<String> selectIds(@Param("request") BaseQueryRequest request);

    List<LoadCaseCountChartResult> countByRequest(LoadCaseCountRequest request);

    List<LoadTestDTO> relevanceLoadList(@Param("request") LoadCaseRequest request);

    List<BaseCase> selectBaseCaseByProjectId(@Param("projectId") String projectId);
}
