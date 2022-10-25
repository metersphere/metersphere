package io.metersphere.reportstatistics.controller;

import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.service.ReportStatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
public class ReportStatShareController {
    @GetMapping(value = "/chart-pic")
    public String getChart() {
        return "share-enterprise-report.html";
    }
}

@RequestMapping(value = "/share/info")
class ShareInfoController {
    @Resource
    private ReportStatisticsService reportStatisticsService;

    @PostMapping("/selectHistoryReportById")
    public ReportStatisticsWithBLOBs selectById(@RequestBody ReportStatisticsSaveRequest request) {
        return reportStatisticsService.selectById(request.getId());
    }
}

