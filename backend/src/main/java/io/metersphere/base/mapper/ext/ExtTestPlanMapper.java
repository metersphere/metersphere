package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtTestPlanMapper {
    List<TestPlanDTOWithMetric> list(@Param("request") QueryTestPlanRequest params);

    List<TestPlanDTOWithMetric> listRelate(@Param("request") QueryTestPlanRequest params);

    List<TestPlanDTO> planList(@Param("request") QueryTestPlanRequest params);

    List<TestPlanDTO> selectByIds(@Param("list") List<String> ids);

    int updatePlan(@Param("plan") TestPlanDTO plan);

    List<TestPlanDTO> selectReference(@Param("request") QueryTestPlanRequest params);

    int checkIsHave(@Param("planId") String planId, @Param("workspaceIds") Set<String> workspaceIds);
}
