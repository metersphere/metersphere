package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.project.dto.ModuleCountDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanMapper {
    List<String> selectByGroupId(String parentId);

    List<String> selectByGroupIdList(@Param("list") List<String> parentTestPlanId);

    List<TestPlanResponse> selectByConditions(@Param("request") TestPlanTableRequest request,@Param("groupIds") List<String> groupIds);

    List<String> selectIdByConditions(TestPlanQueryConditions testPlanQueryConditions);

    List<String> selectGroupIdByConditions(TestPlanQueryConditions testPlanQueryConditions);

    List<ModuleCountDTO> countModuleIdByConditions(TestPlanQueryConditions testPlanQueryConditions);

    List<TestPlan> selectBaseInfoByIds(@Param("list") List<String> deleteIdList);

    long updateDefaultGroupId(@Param("list") List<String> groupIds);

    String selectProjectIdByTestPlanId(String testPlanId);
}
