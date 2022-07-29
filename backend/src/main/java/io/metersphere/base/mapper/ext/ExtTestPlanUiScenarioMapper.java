package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanUiScenario;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanUiScenarioMapper {

    List<TestPlanUiScenario> selectPlanByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids);


}