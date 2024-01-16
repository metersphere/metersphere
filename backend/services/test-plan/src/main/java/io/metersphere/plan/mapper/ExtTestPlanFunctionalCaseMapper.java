package io.metersphere.plan.mapper;

import io.metersphere.plan.dto.AssociationNode;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanFunctionalCaseMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByTestPlanId(String testPlanId);

    List<String> getIdByIds(@Param("selectIds") List<String> selectIds, @Param("testPlanId") String testPlanId, @Param("repeatCase") boolean repeatCase, @Param("orderString") String orderString);

    List<String> getIdByModuleIds(@Param("moduleIds") List<String> selectModuleIds, @Param("testPlanId") String testPlanId, @Param("repeatCase") boolean repeatCase, @Param("orderString") String orderString);

    AssociationNode selectDragInfoById(String id);

    AssociationNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
