package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.service.TestCaseService;
import io.metersphere.user.SessionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return testCaseService.recentTestPlans(request, count);
    }

    @PostMapping("/list")
    public List<TestCase> getTestCaseByNodeId(@RequestBody List<Integer> nodeIds){
        return testCaseService.getTestCaseByNodeId(nodeIds);
    }

    @PostMapping("/name")
    public List<TestCase> getTestCaseNames(@RequestBody QueryTestCaseRequest request){
        return testCaseService.getTestCaseNames(request);
    }

    @GetMapping("/get/{testCaseId}")
    public TestCaseWithBLOBs getTestCase(@PathVariable String testCaseId){
        return testCaseService.getTestCase(testCaseId);
    }

    @GetMapping("/project/{testCaseId}")
    public Project getProjectByTestCaseId(@PathVariable String testCaseId){
        return testCaseService.getProjectByTestCaseId(testCaseId);
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

    @PostMapping("/import/{projectId}")
    public ExcelResponse testCaseImport(MultipartFile file, @PathVariable String projectId){
        return testCaseService.testCaseImport(file, projectId);
    }


}
