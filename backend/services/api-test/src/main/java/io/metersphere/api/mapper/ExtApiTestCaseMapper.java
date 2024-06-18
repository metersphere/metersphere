package io.metersphere.api.mapper;


import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.scenario.ScenarioSystemRequest;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiTestCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    List<ApiTestCaseDTO> listByRequest(@Param("request") ApiTestCasePageRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat, @Param("testPlanId") String testPlanId);

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

    List<TestCaseProviderDTO> listByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(@Param("projectId") String projectId);

    List<ApiTestCase> getTestCaseByProvider(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);


    List<ApiTestCase> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ExecuteReportDTO> getExecuteList(@Param("request") ExecutePageRequest request);

    List<OptionDTO> selectVersionOptionByIds(@Param("ids") List<String> ids);

    List<String> getIdsByModules(@Param("request") ScenarioSystemRequest caseRequest);

    List<ApiTestCase> getApiCaseDefinitionInfo(@Param("ids") List<String> ids);

    void updatePos(String id, long pos);

    List<String> selectIdByProjectIdOrderByPos(String projectId);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<ApiTestCase> getApiCaseExecuteInfoByIds(@Param("ids") List<String> ids);

    /**
     * 获取缺陷未关联的接口用例列表
     *
     * @param request provider参数
     * @param deleted 是否删除状态
     * @param sort    排序
     * @return 通用的列表Case集合
     */
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
}