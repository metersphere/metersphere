package io.metersphere.plan.controller;

import io.metersphere.plan.dto.request.PlanPerformanceExecRequest;
import io.metersphere.plan.service.PerfExecService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/performance/exec")
public class PerfExecController {
    @Resource
    PerfExecService perfExecService;

    @PostMapping("/run")
    public Map<String, String> run(@RequestBody PlanPerformanceExecRequest request) {
      return perfExecService.run(request.getPlanReportId(), request.getConfig(), request.getTriggerMode(), request.getPerfMap());
    }
}
