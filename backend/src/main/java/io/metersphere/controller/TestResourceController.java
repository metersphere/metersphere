package io.metersphere.controller;

import io.metersphere.base.domain.TestResource;
import io.metersphere.service.TestResourceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("testresource")
@RestController
public class TestResourceController {

    @Resource
    private TestResourceService testResourceService;

    @PostMapping("/add")
    public TestResource addTestResource(@RequestBody TestResource testResource) {
        return testResourceService.addTestResource(testResource);
    }

    @GetMapping("/list/{testResourcePoolId}")
    public List<TestResource> getTestResourceList(@PathVariable(value = "testResourcePoolId") String testResourcePoolId) {
        return testResourceService.getTestResourceList(testResourcePoolId);
    }

    @GetMapping("/delete/{testResourceId}")
    public void deleteTestResource(@PathVariable(value = "testResourceId") String testResourceId) {
        testResourceService.deleteTestResource(testResourceId);
    }

    @PostMapping("/update")
    public void updateTestResource(@RequestBody TestResource testResource) {
        testResourceService.updateTestResource(testResource);
    }
}
