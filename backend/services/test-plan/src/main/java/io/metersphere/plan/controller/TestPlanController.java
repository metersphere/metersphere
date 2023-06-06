package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.validation.groups.Created;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-plan")
public class TestPlanController {
    @Resource
    private TestPlanService testPlanService;

    @PostMapping("/add")
    public TestPlanDTO addUser(@Validated({Created.class}) @RequestBody TestPlanDTO testPlan) {
        return testPlanService.add(testPlan);
    }

    @PostMapping("/delete/batch")
    public void deleteBatch(@RequestBody List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            MSException.throwException("The ids cannot be empty!");
        }
        testPlanService.batchDelete(idList);
    }

    @GetMapping("/delete/{id}")
    public void delete(@NotBlank @PathVariable String id) {
        testPlanService.delete(id);
    }
}
