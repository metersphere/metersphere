package io.metersphere.controller;

import io.metersphere.dto.ResourcePoolOperationInfo;
import io.metersphere.request.NodeOperationSelectRequest;
import io.metersphere.service.PrometheusService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prometheus")
public class PrometheusController {

    @Resource
    private PrometheusService prometheusService;

    @PostMapping("/query/node-operation-info")
    public List<ResourcePoolOperationInfo> queryMetric(@RequestBody NodeOperationSelectRequest request) {
        return prometheusService.queryNodeOperationInfo(request);
    }
}
