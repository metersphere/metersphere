package io.metersphere.plan.mapper;

import io.metersphere.plan.dto.AssociationNode;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiCaseMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByTestPlanId(String testPlanId);

    List<String> getIdByIds(ResourceSelectParam resourceSelectParam);

    List<String> getIdByModuleIds(ResourceSelectParam resourceSelectParam);

    AssociationNode selectDragInfoById(String id);

    AssociationNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
