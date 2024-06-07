package io.metersphere.plan.mapper;

import io.metersphere.plan.dto.TestPlanCollectionConfigDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanCollectionMapper {

    List<TestPlanCollectionConfigDTO> getList(@Param("planId") String planId);

}
