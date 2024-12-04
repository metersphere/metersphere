package io.metersphere.api.mapper;


import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.ApiResourceBatchRunInfo;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.scenario.ScenarioSystemRequest;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.*;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiTestCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    @BaseConditionFilter
    List<ApiTestCaseDTO> listByRequest(@Param("request") ApiTestCasePageRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat, @Param("testPlanId") String testPlanId);

    @BaseConditionFilter
    List<String> getIds(@Param("request") ApiTestCaseBatchRequest request, @Param("deleted") boolean deleted);

    void batchMoveGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("deleteTime") long deleteTime);

    List<ApiTestCase> getCaseInfoByApiIds(@Param("ids") List<String> apiIds, @Param("deleted") boolean deleted);

    List<ApiTestCase> getCaseInfoByIds(@Param("ids") List<String> caseIds, @Param("deleted") boolean deleted);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    List<CasePassDTO> findPassRateByIds(@Param("ids") List<String> ids);

    List<String> selectIdsByCaseIds(@Param("ids") List<String> ids);

    List<String> getCaseIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getIdsByApiIds(@Param("ids") List<String> ids);

    @BaseConditionFilter
    List<TestCaseProviderDTO> listByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(@Param("projectId") String projectId);

    List<ApiTestCase> getTestCaseByProvider(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    List<ApiTestCase> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiTestCase> getNameInfo(@Param("ids") List<String> ids);

    List<ExecuteReportDTO> getExecuteList(@Param("request") ExecutePageRequest request);

    List<OptionDTO> selectVersionOptionByIds(@Param("ids") List<String> ids);

    List<String> getIdsByModules(@Param("request") ScenarioSystemRequest caseRequest);

    List<ApiTestCase> getApiCaseDefinitionInfo(@Param("ids") List<String> ids);

    void updatePos(String id, long pos);

    List<String> selectIdByProjectIdOrderByPos(String projectId);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<ApiResourceBatchRunInfo> getApiCaseExecuteInfoByIds(@Param("ids") List<String> ids);

    /**
     * 获取缺陷未关联的接口用例列表
     *
     * @param request provider参数
     * @param deleted 是否删除状态
     * @param sort    排序
     * @return 通用的列表Case集合
     */
    @BaseConditionFilter
    List<TestCaseProviderDTO> listUnRelatedCaseWithBug(@Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted, @Param("sort") String sort);

    /**
     * 根据关联条件获取关联的用例ID
     *
     * @param request 关联参数
     * @param deleted 是否删除状态
     * @return 关联的用例ID集合
     */
    List<String> getSelectIdsByAssociateParam(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    List<ExecuteReportDTO> getTestPlanNum(@Param("ids") List<String> ids);

    ApiTestCase getCaseByResourceId(String resourceId);

    ApiTestCase getCaseByReportId(String resourceId);

    List<ApiTestCase> selectAllApiCase(@Param("isRepeat") boolean isRepeat, @Param("projectId") String projectId, @Param("testPlanId") String testPlanId, @Param("protocols") List<String> protocols);

    List<ApiTestCase> getListBySelectModules(@Param("isRepeat") boolean isRepeat, @Param("projectId") String projectId, @Param("moduleIds") List<String> moduleIds, @Param("testPlanId") String testPlanId, @Param("protocols") List<String> protocols);

    List<ApiTestCase> getListBySelectIds(@Param("projectId") String projectId, @Param("ids") List<String> ids, @Param("testPlanId") String testPlanId, @Param("protocols") List<String> protocols);

    List<ApiTestCaseAssociateDTO> selectAllApiCaseWithAssociate(@Param("projectId") String projectId, @Param("protocols") List<String> protocols);

    List<ApiTestCaseAssociateDTO> getListBySelectModulesWithAssociate(@Param("projectId") String projectId, @Param("moduleIds") List<String> moduleIds, @Param("protocols") List<String> protocols);

    List<ApiTestCaseAssociateDTO> getListBySelectIdsWithAssociate(@Param("projectId") String projectId, @Param("ids") List<String> ids, @Param("protocols") List<String> protocols);

    List<ApiTestCase> getCaseListBySelectIds(@Param("isRepeat") boolean isRepeat, @Param("projectId") String projectId, @Param("ids") List<String> ids, @Param("testPlanId") String testPlanId, @Param("protocols") List<String> protocols);

    void setApiChangeByApiDefinitionId(@Param("apiDefinitionId") String apiDefinitionId);

    List<ApiTestCase> getRefApiScenarioCreator(@Param("ids") List<String> caseIds);

    void clearApiChange(@Param("ids") List<String> ids);

    List<ApiTestCaseWithBlob> selectAllDetailByApiIds(@Param("apiIds") List<String> apiIds);

    List<ApiTestCaseWithBlob> selectAllDetailByIds(@Param("ids") List<String> apiIds);

    List<ApiTestCaseDTO> selectBaseInfoByProjectIdAndApiId(@Param("projectId") String projectId, @Param("apiId") String apiId);

    List<ProjectCountDTO> projectApiCaseCount(@Param("projectIds") Set<String> projectIds, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userId") String userId);

    List<ProjectUserCreateCount> userCreateApiCaseCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") Set<String> userIds);

    List<ApiTestCase> getSimpleApiCaseList(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);


    List<String> selectApiId(String projectId);

    List<String> selectApiIdByProjectAndProtocol(@Param("projectId") String projectId, @Param("protocols") List<String> protocols);

    List<String> selectApiIdByCaseId(@Param("ids") List<String> apiCaseIdInStep, @Param("protocols") List<String> apiProtocols, @Param("ignoreApiIds") List<String> ignoreApiIds);
}