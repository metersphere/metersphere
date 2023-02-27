package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.plan.dto.CaseExecResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtTestPlanUiCaseMapper {
    @Select("SELECT id,test_plan_id,ui_scenario_id,last_result FROM test_plan_ui_scenario WHERE id = #{0} ")
    TestPlanUiScenario selectBaseInfoById(String testId);

    List<CaseExecResult> selectExecResult(@Param("testPlanId") String testPlanId, @Param("ids") List<String> relevanceApiCaseList);
}
