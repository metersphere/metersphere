package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.project.dto.DropNode;
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
}
