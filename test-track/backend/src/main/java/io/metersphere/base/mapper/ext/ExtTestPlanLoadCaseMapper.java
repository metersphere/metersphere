package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.plan.dto.CaseExecResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtTestPlanLoadCaseMapper {
    @Select("SELECT id,test_plan_id,load_case_id,status,load_report_id FROM test_plan_load_case WHERE id = #{0} ")
    TestPlanLoadCase selectBaseInfoById(String testId);

    List<CaseExecResult> selectExecResult(@Param("testPlanId") String testPlanId, @Param("ids") List<String> relevanceApiCaseList);

    @Select("select status from load_test_report where id = #{0}")
    String queryReportStatus(String reportId);
}
