package io.metersphere.plan.mapper;

import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.plan.dto.AssociationNode;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.TestPlanApiRequest;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiCaseMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByTestPlanId(String testPlanId);

    List<String> getIdByParam(ResourceSelectParam resourceSelectParam);

    AssociationNode selectDragInfoById(String id);

    AssociationNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<TestPlanCaseRunResultCount> selectCaseExecResultCount(String testPlanId);

    List<ApiDefinitionDTO> list(@Param("request") TestPlanApiRequest request, @Param("isRepeat") boolean isRepeat);
}
