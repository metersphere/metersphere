package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanApiCaseDTO;
import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-plan/api/case")
public class TestPlanApiCaseController {

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
}
