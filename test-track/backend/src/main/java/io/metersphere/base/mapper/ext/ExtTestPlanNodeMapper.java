package io.metersphere.base.mapper.ext;

import io.metersphere.dto.TestPlanNodeDTO;
import io.metersphere.plan.request.QueryTestPlanRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanNodeMapper {

    List<TestPlanNodeDTO> getCountNodes(@Param("request") QueryTestPlanRequest request);

    List<TestPlanNodeDTO> getNodeTreeByProjectId(@Param("projectId") String projectId);

    TestPlanNodeDTO getNode(String id);

    void updatePos(String id, Double pos);
}
