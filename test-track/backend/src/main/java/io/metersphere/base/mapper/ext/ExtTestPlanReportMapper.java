package io.metersphere.base.mapper.ext;

import io.metersphere.dto.ParamsDTO;
import io.metersphere.dto.TestPlanReportDTO;
import io.metersphere.request.report.QueryTestPlanReportRequest;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author song.tianyang
 * @Date 2021/1/8 4:58 下午
 * @Description
 */
public interface ExtTestPlanReportMapper {
    List<TestPlanReportDTO> list(@Param("request") QueryTestPlanReportRequest request);

    @MapKey("id")
    Map<String, ParamsDTO> reportCount(@Param("planIds") Set<String> planIds);

    void setApiBaseCountAndPassRateIsNullById(String id);

    void updateAllStatus();

    void deleteApiReportByTestPlanReportList(@Param("ids") List<String> testPlanReportIdList);

    List<String> selectScenarioReportByTestPlanReportIds(@Param("ids") List<String> testPlanReportIdList);

    List<String> selectUiReportByTestPlanReportIds(@Param("ids") List<String> testPlanReportIdList);

    void deleteScenarioReportByIds(@Param("ids") List<String> scenarioReportIds);

    void deleteScenarioReportResultByIds(@Param("ids") List<String> scenarioReportIds);

    void deleteScenarioReportStructureByIds(@Param("ids") List<String> scenarioReportIds);

    void deleteUiReportByIds(@Param("ids") List<String> scenarioReportIds);

    void deleteUiReportResultByIds(@Param("ids") List<String> scenarioReportIds);

    void deleteUiReportStructureByIds(@Param("ids") List<String> scenarioReportIds);


}
