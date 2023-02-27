package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.CaseExecResult;
import io.metersphere.plan.dto.TestPlanApiScenarioInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtTestPlanScenarioCaseMapper {
    List<TestPlanApiScenarioInfoDTO> selectLegalDataByTestPlanId(String planId);

    List<TestPlanApiScenarioInfoDTO> selectLegalUiDataByTestPlanId(String planId);

    List<CaseExecResult> selectExecResult(@Param("testPlanId") String testPlanId, @Param("scenarioCaseIds") List<String> loadCaseIdList);

    @Select("SELECT id,test_plan_id,api_scenario_id,last_result FROM test_plan_api_scenario WHERE id = #{0} ")
    TestPlanApiScenario selectBaseInfoById(String testId);
}
