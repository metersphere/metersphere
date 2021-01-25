package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.dto.TestPlanReportDTO;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/1/8 2:38 下午
 * @Description
 */
@RequestMapping("/test/plan/report")
@RestController
public class TestPlanReportController {

    @Resource
    private TestPlanReportService testPlanReportService;
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanReportDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanReportRequest request) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        request.setWorkspaceId(currentWorkspaceId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanReportService.list(request));
    }
    @GetMapping("/getMetric/{planId}")
    public TestPlanReportDTO getMetric(@PathVariable String planId) {
        return testPlanReportService.getMetric(planId);
    }

    @GetMapping("/sendTask/{planId}")
    public String sendTask(@PathVariable String planId) {
        TestPlanReport report = testPlanReportService.getTestPlanReport(planId);
        testPlanReportService.update(report);
        return  "sucess";
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<String> testPlanReportIdList) {
        testPlanReportService.delete(testPlanReportIdList);
    }

    @PostMapping("/deleteBatchByParams")
    public void deleteBatchByParams(@RequestBody QueryTestPlanReportRequest request) {
        testPlanReportService.delete(request);
    }


    @GetMapping("/apiExecuteFinish/{planId}/{userId}")
    public void apiExecuteFinish(@PathVariable String planId,@PathVariable String userId) {
        TestPlanReport report = testPlanReportService.genTestPlanReport(planId,userId,ReportTriggerMode.API.name());
        testPlanReportService.countReportByTestPlanReportId(report.getId(),null, ReportTriggerMode.API.name());
    }

    @GetMapping("/saveTestPlanReport/{planId}/{triggerMode}")
    public String saveTestPlanReport(@PathVariable String planId,@PathVariable String triggerMode) {
        String userId = SessionUtils.getUser().getId();
        TestPlanReport report = testPlanReportService.genTestPlanReport(planId,userId,triggerMode);
        testPlanReportService.countReportByTestPlanReportId(report.getId(),null, triggerMode);
        return "success";
    }
}
