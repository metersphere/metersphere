package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanReportResource;

import java.util.List;

public interface ExtTestPlanReportResourceMapper {
    List<TestPlanReportResource> selectResourceIdAndResourceTypeAndExecuteResultByTestPlanReportId(String testPlanReportId);
}
