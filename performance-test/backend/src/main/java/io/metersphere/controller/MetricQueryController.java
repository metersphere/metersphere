package io.metersphere.controller;

import io.metersphere.dto.MetricData;
import io.metersphere.dto.Monitor;
import io.metersphere.service.MetricQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/metric")
public class MetricQueryController {

    @Resource
    private MetricQueryService metricService;

    @GetMapping("/query/{id}")
    public List<MetricData> queryMetric(@PathVariable("id") String reportId) {
        return metricService.queryMetric(reportId);
    }

    @GetMapping("/query/resource/{id}")
    public List<Monitor> queryReportResource(@PathVariable("id") String reportId) {
        return metricService.queryReportResource(reportId);
    }
}
