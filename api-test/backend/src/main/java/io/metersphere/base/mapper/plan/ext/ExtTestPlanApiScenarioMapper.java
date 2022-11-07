package io.metersphere.base.mapper.plan.ext;

import io.metersphere.base.domain.TestPlanApiScenario;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiScenarioMapper {
    List<TestPlanApiScenario> selectByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids);

    List<TestPlanApiScenario> selectPlanByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids);

    List<TestPlanApiScenario> selectByScenarioIds(@Param("ids") List<String> ids);
}
