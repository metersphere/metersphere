package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanLoadCaseMapper {

    List<String> selectIdsNotInPlan(@Param("projectId") String projectId, @Param("planId") String planId);
}
