package io.metersphere.plan.mapper;

import io.metersphere.plan.dto.TestPlanCollectionConfigDTO;
import io.metersphere.plan.dto.TestPlanCollectionEnvDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanCollectionMapper {

    List<TestPlanCollectionConfigDTO> getList(@Param("planId") String planId);

    List<TestPlanCollectionEnvDTO> selectSecondCollectionEnv(@Param("type") String type, @Param("parentId") String parentId);

}
