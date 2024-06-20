package io.metersphere.plan.mapper;

import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.ApiScenarioModuleDTO;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.BasePlanCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioBatchRunRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioModuleRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiScenarioMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByRangeId(String rangeId);

    List<String> getIdByParam(ResourceSelectParam resourceSelectParam);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<TestPlanCaseRunResultCount> selectCaseExecResultCount(String testPlanId);

    List<TestPlanApiScenario> selectByTestPlanIdAndNotDeleted(String testPlanId);

    List<TestPlanApiScenarioPageResponse> relateApiScenarioList(@Param("request") TestPlanApiScenarioRequest request, @Param("deleted") boolean deleted);

    List<FunctionalCaseModuleCountDTO> countModuleIdByRequest(@Param("request") TestPlanApiScenarioModuleRequest request, @Param("deleted") boolean deleted);

    long caseCount(@Param("request") TestPlanApiScenarioModuleRequest request, @Param("deleted") boolean deleted);

    List<String> selectIdByProjectIdAndTestPlanId(@Param("projectId") String projectId, @Param("testPlanId") String testPlanId);

    List<ModuleCountDTO> collectionCountByRequest(@Param("request") TestPlanApiScenarioModuleRequest request);

    List<ProjectOptionDTO> selectRootIdByTestPlanId(@Param("testPlanId") String testPlanId);

    List<ApiScenarioModuleDTO> selectBaseByProjectIdAndTestPlanId(@Param("testPlanId") String testPlanId);

    /**
     * 获取计划下的功能用例集合
     *
     * @param planIds 测试计划ID集合
     * @return 计划功能用例集合
     */
    List<TestPlanApiScenario> getPlanApiScenarioByIds(@Param("planIds") List<String> planIds);

    List<TestPlanApiScenario> getScenarioExecuteInfoByIds(@Param("ids") List<String> ids);

    List<String> getIds(@Param("request") BasePlanCaseBatchRequest request, @Param("deleted") boolean deleted);

    void batchUpdateExecutor(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<TestPlanApiScenario> getSelectIdAndCollectionId(@Param("request")  TestPlanApiScenarioBatchRunRequest request);

    List<String> getIdsByReportIdAndCollectionId(@Param("testPlanReportId") String testPlanReportId, @Param("collectionId") String collectionId);
}
