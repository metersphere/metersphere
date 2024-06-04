package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtRealMapper {
    int caseReportCountByProjectIds(@Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);

    int scenarioReportCountByProjectIds(@Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);

    int testPlanReportCountByProjectIds(@Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);
}
