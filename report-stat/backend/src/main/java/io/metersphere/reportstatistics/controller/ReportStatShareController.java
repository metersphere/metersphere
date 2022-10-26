package io.metersphere.reportstatistics.controller;

import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.service.ReportStatisticsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/report/stat/share")
public class ReportStatShareController {
    @Resource
    private ReportStatisticsService reportStatisticsService;

    @PostMapping("/select/report/by/id")
    public ReportStatisticsWithBLOBs selectById(@RequestBody ReportStatisticsSaveRequest request) {
        return reportStatisticsService.selectById(request.getId());
    }
}