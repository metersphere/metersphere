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

    @PostMapping("/add")
    public void add(@Validated({Created.class}) @RequestBody TestPlanApiCaseDTO testPlanApiCaseDTO) {
        testPlanApiCaseService.add(testPlanApiCaseDTO);
    }

    @PutMapping("/update")
    public void update(@Validated({Updated.class}) @RequestBody TestPlanApiCaseDTO testPlanApiCaseDTO) {
        testPlanApiCaseService.update(testPlanApiCaseDTO);
    }

    @DeleteMapping("/delete/{id}")
    public int delete(@PathVariable String id) {
        return testPlanApiCaseService.delete(id);
    }

    @GetMapping("/get/{id}")
    public TestPlanApiCaseDTO get(@PathVariable String id) {
        return testPlanApiCaseService.get(id);
    }
}
