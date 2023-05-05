package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.ApiCountChartResult;
import io.metersphere.api.dto.ApiCountRequest;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExampleWithOperation;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioRequest request);

    List<ApiScenarioDTO> selectIds(@Param("ids") List<String> ids);

    int selectTrash(@Param("projectId") String projectId);

    List<ApiScenarioWithBLOBs> selectByIds(@Param("ids") String ids, @Param("order") String order);

    List<ApiScenarioDTO> selectReference(@Param("request") ApiScenarioRequest request);

    int removeToGcByExample(ApiScenarioExampleWithOperation example);

    int reduction(@Param("ids") List<String> ids);

    long countByProjectID(@Param("projectId") String projectId, @Param("versionId") String versionId);

    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("versionId") String versionId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countRunResultByProjectID(@Param("projectId") String projectId, @Param("versionId") String versionId);

    ApiScenario getNextNum(@Param("projectId") String projectId);

    List<String> selectIdsByQuery(@Param("request") BaseQueryRequest request);

    List<ApiScenarioWithBLOBs> listWithIds(@Param("ids") List<String> ids);

    List<ApiScenarioWithBLOBs> listWithRefIds(@Param("ids") List<String> ids);

    List<ApiScenarioModuleDTO> listModuleByCollection(@Param("request") ApiScenarioRequest request);

    void checkOriginalStatusByIds(@Param("ids") List<String> ids);

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    List<RelationshipGraphData.Node> getTestCaseForGraph(@Param("ids") Set<String> ids);

    ApiScenarioDTO selectById(@Param("id") String id);

    List<ApiScenarioDTO> selectByScenarioIds(@Param("ids") List<String> ids);

    void clearLatestVersion(String refId);

    void addLatestVersion(String refId);

    void updateVersionModule(@Param("refId") String refId, @Param("versionId") String versionId, @Param("moduleId") String moduleId, @Param("modulePath") String modulePath);

    List<ApiScenarioWithBLOBs> selectRepeatByBLOBs(@Param("names") List<String> names, @Param("projectId") String projectId);

    List<String> selectRelevanceIdsByQuery(@Param("request") BaseQueryRequest query);

    void updateNoModuleToDefaultModule(@Param("projectId") String projectId, @Param("status") String status, @Param("moduleId") String moduleId);

    List<ApiScenario> selectBaseInfoByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") String status);

    List<ApiScenario> selectBaseInfoByCondition(@Param("projectId") String projectId, @Param("status") String status, @Param("request") ApiScenarioRequest request);

    List<ApiCountChartResult> countByRequest(ApiCountRequest request);

    List<ApiScenarioDTO> relevanceScenarioList(@Param("request") ApiScenarioRequest request);

    List<BaseCase> selectBaseCaseByProjectId(@Param("projectId") String projectId);

    List<String> selectScenarioIdInExecutionInfoByProjectIdIsNull();

    long countSourceIdByProjectIdIsNull();

    List<String> selectIdByScenarioRequest(@Param("request") ApiScenarioRequest request);
}
