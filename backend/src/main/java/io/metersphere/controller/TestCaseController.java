package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.dto.TestPlanCaseDTO;
import io.metersphere.service.TestCaseNodeService;
import io.metersphere.service.TestCaseService;
import io.metersphere.user.SessionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/case")
@RestController
public class TestCaseController {

    @Resource
    TestCaseService testCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestCaseWithBLOBs>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCase(request));
    }

    @GetMapping("recent/{count}")
    public List<TestCase> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count, true);
        return testCaseService.recentTestPlans(request);
    }

    @PostMapping("/list")
    public List<TestCase> getTestCaseByNodeId(@RequestBody List<Integer> nodeIds){
        return testCaseService.getTestCaseByNodeId(nodeIds);
    }

    @PostMapping("/name")
    public List<TestCase> getTestCaseNames(@RequestBody QueryTestCaseRequest request){
        return testCaseService.getTestCaseNames(request);
    }

    @PostMapping("/get/{testCaseId}")
    public List<TestCaseWithBLOBs> getTestCase(@PathVariable String testCaseId){
        return testCaseService.getTestCase(testCaseId);
    }

    @PostMapping("/add")
    public void addTestCase(@RequestBody TestCaseWithBLOBs testCase){
        testCaseService.addTestCase(testCase);
    }

    @PostMapping("/edit")
    public void editTestCase(@RequestBody TestCaseWithBLOBs testCase){
        testCaseService.editTestCase(testCase);
    }

    @PostMapping("/delete/{testCaseId}")
    public int deleteTestCase(@PathVariable String testCaseId){
        return testCaseService.deleteTestCase(testCaseId);
    }


}
