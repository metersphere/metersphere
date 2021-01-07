package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanLoadCaseMapper {

    List<String> selectIdsNotInPlan(@Param("projectId") String projectId, @Param("planId") String planId);
    List<TestPlanLoadCaseDTO> selectTestPlanLoadCaseList(@Param("planId") String planId);
}
