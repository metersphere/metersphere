package io.metersphere.controller;

import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.service.TestResourcePoolService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RequestMapping("testresourcepool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    public TestResourcePool addTestResourcePool(@RequestBody TestResourcePool testResourcePool) {
        return testResourcePoolService.addTestResourcePool(testResourcePool);
    }

    @GetMapping("/delete/{testResourcePoolId}")
    public void deleteTestResourcePool(@PathVariable(value = "testResourcePoolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    public void updateTestResourcePool(@RequestBody TestResourcePool testResourcePool) {
        testResourcePoolService.updateTestResourcePool(testResourcePool);
    }

    @GetMapping("/list")
    public List<TestResourcePool> getTestResourcePoolList() { return testResourcePoolService.getTestResourcePoolList(); }
}
