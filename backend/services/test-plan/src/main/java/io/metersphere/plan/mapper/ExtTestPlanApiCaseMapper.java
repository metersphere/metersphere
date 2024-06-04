package io.metersphere.plan.mapper;

import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.request.TestPlanApiRequest;
import io.metersphere.plan.dto.response.TestPlanApiCasePageResponse;
import io.metersphere.project.dto.DropNode;
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
}
