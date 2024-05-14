package io.metersphere.plan.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanCaseExecuteHistoryMapper {

    void updateDeleted(@Param("testPlanCaseIds") List<String> testPlanCaseIds, @Param("deleted") boolean deleted);
}
