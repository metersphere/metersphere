package io.metersphere.controller;

import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.plan.service.TestPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ShareReportController {

    @Resource
    TestPlanService testPlanService;


    @NoResultHolder
    @GetMapping(value = "/share-plan-report")
    public String shareRedirect() {
        return testPlanService.getShareReport();
    }
}
