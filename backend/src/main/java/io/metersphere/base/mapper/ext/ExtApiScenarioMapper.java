package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExampleWithOperation;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.dto.RelationshipGraphData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioRequest request);

    int listModule(@Param("request") ApiScenarioRequest request);

    List<ApiScenarioDTO> listReview(@Param("request") ApiScenarioRequest request);

    List<ApiScenarioWithBLOBs> selectByTagId(@Param("id") String id);

    List<ApiScenarioDTO> selectIds(@Param("ids") List<String> ids);

    int selectTrash(@Param("projectId") String projectId);

    List<ApiScenarioWithBLOBs> selectByIds(@Param("ids") String ids, @Param("oderId") String oderId);

    List<ApiScenario> selectReference(@Param("request") ApiScenarioRequest request);

    int removeToGcByExample(ApiScenarioExampleWithOperation example);

    int reduction(@Param("ids") List<String> ids);

    long countByProjectID(String projectId);

    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countRunResultByProjectID(String projectId);

    List<String> selectIdsNotExistsInPlan(String projectId, String planId);

    ApiScenario getNextNum(@Param("projectId") String projectId);

    List<String> selectIdsByQuery(@Param("request") BaseQueryRequest request);

    List<String> selectIdsByProjectId(String projectId);

    void updateCustomNumByProjectId(@Param("projectId") String projectId);

    List<ApiScenarioWithBLOBs> listWithIds(@Param("ids") List<String> ids);

    List<ApiScenarioWithBLOBs> listWithRefIds(@Param("ids") List<String> ids);

    List<Map<String, Object>> listModuleByCollection(@Param("request") ApiScenarioRequest request);

    String selectNameById(String id);

    List<String> selectNameByIdIn(@Param("ids") List<String> id);

    List<ApiScenarioWithBLOBs> selectByNoReferenceId();

    void checkOriginalStatusByIds(@Param("ids") List<String> ids);

    List<String> selectIdsByExecuteTimeIsNull();

    Long countExecuteTimesByProjectID(String projectId);

    List<String> selectProjectIds();

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    List<RelationshipGraphData.Node> getTestCaseForGraph(@Param("ids") Set<String> ids);

    void setScenarioEnvGroupIdNull(@Param("environmentGroupId") String environmentGroupId);

    ApiScenarioDTO selectById(@Param("id") String id);

    void clearLatestVersion(String refId);

    void addLatestVersion(String refId);

    List<String> selectRefIdsForVersionChange(@Param("versionId") String versionId, @Param("projectId") String projectId);

    List<ApiScenarioWithBLOBs> selectByStatusIsNotTrash();

    List<ApiScenarioWithBLOBs> selectRepeatByBLOBs(@Param("names") List<String> names, @Param("projectId") String projectId);

    List<String> selectRelevanceIdsByQuery(@Param("request") BaseQueryRequest query);

    void updateNoModuleToDefaultModule(@Param("projectId") String projectId, @Param("status") String status, @Param("moduleId") String moduleId);

    List<ApiScenario> selectBaseInfoByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") String status);
}
