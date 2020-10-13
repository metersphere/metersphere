package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.dto.TestPlanDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanMapper {
    List<TestPlanDTO> list(@Param("request") QueryTestPlanRequest params);

    List<TestPlanDTOWithMetric> listRelate(@Param("request") QueryTestPlanRequest params);

    List<TestPlanDTO> planList(@Param("request") QueryTestPlanRequest params);
}
