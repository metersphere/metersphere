package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTestReportLog;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.*;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.service.*;
import io.metersphere.xpack.resourcepool.engine.provider.ClientCredential;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("share")
public class ShareController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    PerformanceReportService performanceReportService;
    @Resource
    PerformanceTestService performanceTestService;
    @Resource
    MetricQueryService metricService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;


    @GetMapping("/performance/report/{shareId}/{reportId}")
    public ReportDTO getLoadTestReport(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportTestAndProInfo(reportId);
    }

    @GetMapping("/performance/report/content/report_time/{shareId}/{reportId}")
    public ReportTimeInfo getReportTimeInfo(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportTimeInfo(reportId);
    }


    @GetMapping("/performance/report/get-advanced-config/{shareId}/{reportId}")
    public String getAdvancedConfig(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getAdvancedConfiguration(reportId);
    }

    @GetMapping("/performance/report/get-jmx-content/{reportId}")
    public List<LoadTestExportJmx> getJmxContents(@PathVariable String reportId) {
        return performanceReportService.getJmxContent(reportId);
    }

    @GetMapping("/performance/report/get-jmx-content/{shareId}/{reportId}")
    public LoadTestExportJmx getJmxContent(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getJmxContent(reportId).get(0);
    }

    @GetMapping("/performance/get-jmx-content/{shareId}/{testId}")
    public List<LoadTestExportJmx> getOldJmxContent(@PathVariable String shareId, @PathVariable String testId) {
        shareInfoService.validateExpired(shareId);
        return performanceTestService.getJmxContent(testId);
    }


    @GetMapping("/performance/report/content/testoverview/{shareId}/{reportId}")
    public TestOverview getTestOverview(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getTestOverview(reportId);
    }

    @GetMapping("/performance/report/content/load_chart/{shareId}/{reportId}")
    public List<ChartsData> getLoadChartData(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getLoadChartData(reportId);
    }

    @GetMapping("/performance/report/content/res_chart/{shareId}/{reportId}")
    public List<ChartsData> getResponseTimeChartData(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getResponseTimeChartData(reportId);
    }

    @GetMapping("/performance/report/content/error_chart/{shareId}/{reportId}")
    public List<ChartsData> getErrorChartData(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getErrorChartData(reportId);
    }

    @GetMapping("/performance/report/content/response_code_chart/{shareId}/{reportId}")
    public List<ChartsData> getResponseCodeChartData(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getResponseCodeChartData(reportId);
    }

    @GetMapping("/performance/report/content/{shareId}/{reportKey}/{reportId}")
    public List<ChartsData> getReportChart(@PathVariable String shareId, @PathVariable String reportKey, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportChart(reportKey, reportId);
    }

    @GetMapping("/performance/report/content/{shareId}/{reportId}")
    public List<Statistics> getReportContent(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportStatistics(reportId);
    }

    @GetMapping("/performance/report/content/errors/{shareId}/{reportId}")
    public List<Errors> getReportErrors(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportErrors(reportId);
    }

    @GetMapping("/performance/report/content/errors_top5/{shareId}/{reportId}")
    public List<ErrorsTop5> getReportErrorsTop5(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportErrorsTOP5(reportId);
    }

    @GetMapping("/performance/report/content/errors_samples/{shareId}/{reportId}")
    public SamplesRecord getErrorSamples(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getErrorSamples(reportId);
    }


    @GetMapping("/performance/report/log/resource/{shareId}/{reportId}")
    public List<LogDetailDTO> getResourceIds(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return performanceReportService.getReportLogResource(reportId);
    }

    @GetMapping("/performance/report/log/download/{reportId}/{resourceId}")
    public void downloadLog(@PathVariable String reportId, @PathVariable String resourceId, HttpServletResponse response) throws Exception {
        performanceReportService.downloadLog(response, reportId, resourceId);
    }

    @GetMapping("/performance/report/log/{shareId}/{reportId}/{resourceId}/{goPage}")
    public Pager<List<LoadTestReportLog>> logs(@PathVariable String shareId, @PathVariable String reportId, @PathVariable String resourceId, @PathVariable int goPage) {
        shareInfoService.validateExpired(shareId);
        Page<Object> page = PageHelper.startPage(goPage, 1, true);
        return PageUtils.setPageInfo(page, performanceReportService.getReportLogs(reportId, resourceId));
    }

    @GetMapping("/metric/query/{shareId}/{id}")
    public List<MetricData> queryMetric(@PathVariable String shareId, @PathVariable("id") String reportId) {
        shareInfoService.validateExpired(shareId);
        return metricService.queryMetric(reportId);
    }

    @GetMapping("/metric/query/resource/{shareId}/{id}")
    public List<Monitor> queryReportResource(@PathVariable String shareId, @PathVariable("id") String reportId) {
        shareInfoService.validateExpired(shareId);
        return metricService.queryReportResource(reportId);
    }

    @GetMapping("/performance/report/get-load-config/{shareId}/{testId}")
    public String getLoadConfiguration(@PathVariable String shareId, @PathVariable String testId) {
        shareInfoService.validateExpired(shareId);
        return performanceTestService.getLoadConfiguration(testId);
    }

    @GetMapping("/performance/report/get-load-config/{reportId}")
    public String getLoadConfiguration(@PathVariable String reportId) {
        return performanceReportService.getLoadConfiguration(reportId);
    }

    @GetMapping("/testresourcepool/list/quota/valid")
    public List<TestResourcePoolDTO> getTestResourcePools() {
        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        // 数据脱敏
        // 仅对k8s操作
        List<TestResourcePoolDTO> testResourcePoolDTOS = baseTestResourcePoolService.listResourcePools(resourcePoolRequest);
        testResourcePoolDTOS.stream()
                .filter(testResourcePoolDTO -> StringUtils.equals(ResourcePoolTypeEnum.K8S.name(), testResourcePoolDTO.getType()))
                .forEach(pool -> pool.getResources().forEach(resource -> {
                    String configuration = resource.getConfiguration();
                    Map map = JSON.parseMap(configuration);
                    if (map.containsKey("token")) {
                        map.put("token", "******");
                    }
                    if (map.containsKey("masterUrl")) {
                        map.put("masterUrl", "******");
                    }
                    if (map.containsKey("jobTemplate")) {
                        map.put("jobTemplate", "******");
                    }
                    if (map.containsKey("namespace")) {
                        map.put("namespace", "******");
                    }
                    resource.setConfiguration(JSON.toJSONString(map));
                }));
        return testResourcePoolDTOS;
    }
}

@Controller
class ShareIndexController {
    @GetMapping("/share-report")
    public String sharePerformanceRedirectMicro() {
        return "share-performance-report.html";
    }
}