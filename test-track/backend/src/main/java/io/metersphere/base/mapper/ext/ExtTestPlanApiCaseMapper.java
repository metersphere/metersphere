package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.plan.dto.CaseExecResult;
import io.metersphere.plan.dto.TestPlanApiCaseInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtTestPlanApiCaseMapper {
    List<TestPlanApiCaseInfoDTO> selectLegalDataByTestPlanId(String planId);

    List<CaseExecResult> selectExecResult(@Param("testPlanId") String testPlanId, @Param("apiCaseIds") List<String> apiCaseIdList);

    @Select("SELECT id,test_plan_id,api_case_id,status FROM test_plan_api_case WHERE id = #{0} ")
    TestPlanApiCase selectBaseInfoById(String testId);
}

