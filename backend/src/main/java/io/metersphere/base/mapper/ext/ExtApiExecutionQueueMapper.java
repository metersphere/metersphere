package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiExecutionQueue;

import java.util.List;

public interface ExtApiExecutionQueueMapper {
    void delete();

    List<ApiExecutionQueue> findTestPlanReportQueue();

    List<String> findTestPlanRunningReport();
}