package io.metersphere.plan.mapper;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.dto.ApiCaseModuleDTO;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.TestPlanApiCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseModuleRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.request.TestPlanApiRequest;
import io.metersphere.plan.dto.response.TestPlanApiCasePageResponse;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiCaseMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByTestPlanId(String testPlanId);

    List<String> getIdByParam(ResourceSelectParam resourceSelectParam);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<TestPlanCaseRunResultCount> selectCaseExecResultCount(String testPlanId);

    List<ApiDefinitionDTO> list(@Param("request") TestPlanApiRequest request, @Param("isRepeat") boolean isRepeat);

    List<TestPlanApiCasePageResponse> relateApiCaseList(@Param("request") TestPlanApiCaseRequest request, @Param("deleted") boolean deleted);

    List<FunctionalCaseModuleCountDTO> countModuleIdByRequest(@Param("request") TestPlanApiCaseRequest request, @Param("deleted") boolean deleted);

    List<String> selectIdByProjectIdAndTestPlanId(@Param("projectId") String projectId, @Param("testPlanId") String testPlanId);

    long caseCount(@Param("request") TestPlanApiCaseRequest request, @Param("deleted") boolean deleted);

    List<TestPlanApiCase> selectByTestPlanIdAndNotDeleted(String testPlanId);

    List<ProjectOptionDTO> selectRootIdByTestPlanId(@Param("testPlanId") String testPlanId);

    List<ApiCaseModuleDTO> selectBaseByProjectIdAndTestPlanId(@Param("testPlanId") String testPlanId);

    List<String> getIds(@Param("request") TestPlanApiCaseBatchRequest request, @Param("deleted") boolean deleted);

    void batchUpdateExecutor(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<ModuleCountDTO> collectionCountByRequest(@Param("request") TestPlanApiCaseModuleRequest request);

    Long getMaxPosByCollectionId(String collectionId);

    /**
     * 获取计划下的功能用例集合
     *
     * @param planIds 测试计划ID集合
     * @return 计划功能用例集合
     */
    List<TestPlanApiCase> getPlanApiCaseByIds(@Param("planIds") List<String> planIds);

    List<TestPlanApiCase> getApiCaseExecuteInfoByIds(@Param("ids") List<String> ids);

    List<ApiTestCase> selectApiCaseByDefinitionIds(@Param("ids") List<String> ids, @Param("isRepeat") boolean isRepeat, @Param("testPlanId") String testPlanId);

    List<TestPlanApiCase> getSelectIdAndCollectionId(@Param("request") TestPlanApiCaseBatchRequest request);
}
