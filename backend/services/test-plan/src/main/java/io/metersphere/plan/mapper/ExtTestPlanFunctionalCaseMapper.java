package io.metersphere.plan.mapper;

import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.FunctionalCaseModuleDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.dto.AssociationNode;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.request.BasePlanCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanCaseRequest;
import io.metersphere.plan.dto.response.TestPlanCasePageResponse;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanFunctionalCaseMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByTestPlanId(String testPlanId);

    List<String> getIdByParam(ResourceSelectParam resourceSelectParam);

    AssociationNode selectDragInfoById(String id);

    AssociationNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<TestPlanCasePageResponse> getCasePage(@Param("request") TestPlanCaseRequest request, @Param("deleted") boolean deleted, @Param("sort") String sort);

    List<ProjectOptionDTO> selectRootIdByTestPlanId(@Param("testPlanId") String testPlanId);

    List<FunctionalCaseModuleDTO> selectBaseByProjectIdAndTestPlanId(@Param("testPlanId") String testPlanId);

    List<FunctionalCaseModuleCountDTO> countModuleIdByRequest(@Param("request") TestPlanCaseRequest request, @Param("deleted") boolean deleted);

    long caseCount(@Param("request") TestPlanCaseRequest request, @Param("deleted") boolean deleted);

    List<String> getIds(@Param("request") BasePlanCaseBatchRequest request, @Param("deleted") boolean deleted);

    /**
     * 获取计划下的功能用例集合
     *
     * @param planIds 测试计划ID集合
     * @return 计划功能用例集合
     */
    List<TestPlanFunctionalCase> getPlanFunctionalCaseByIds(@Param("planIds") List<String> planIds);

    List<String> selectIdByConditions(@Param("request") BasePlanCaseBatchRequest request, @Param("projectId") String projectId);

    void batchUpdate(@Param("ids") List<String> ids, @Param("lastExecResult") String lastExecResult, @Param("lastExecTime") long lastExecTime, @Param("userId") String userId);
}
