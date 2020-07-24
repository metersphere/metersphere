package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import io.metersphere.track.service.TestCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/test/case")
@RestController
@RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
public class TestCaseController {

    @Resource
    TestCaseService testCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCase(request));
    }

    @GetMapping("/list/{projectId}")
    public List<TestCaseDTO> list(@PathVariable String projectId) {
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCase(request);
    }


    @GetMapping("/list/method/{projectId}")
    public List<TestCaseDTO> listByMethod(@PathVariable String projectId) {
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCaseMthod(request);
    }


    @GetMapping("recent/{count}")
    public List<TestCase> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setWorkspaceId(currentWorkspaceId);
        return testCaseService.recentTestPlans(request, count);
    }

    @PostMapping("/list")
    public List<TestCase> getTestCaseByNodeId(@RequestBody List<String> nodeIds) {
        return testCaseService.getTestCaseByNodeId(nodeIds);
    }

    @PostMapping("/name")
    public List<TestCase> getTestCaseNames(@RequestBody QueryTestCaseRequest request) {
        return testCaseService.getTestCaseNames(request);
    }

    @GetMapping("/get/{testCaseId}")
    public TestCaseWithBLOBs getTestCase(@PathVariable String testCaseId) {
        return testCaseService.getTestCase(testCaseId);
    }

    @GetMapping("/project/{testCaseId}")
    public Project getProjectByTestCaseId(@PathVariable String testCaseId) {
        return testCaseService.getProjectByTestCaseId(testCaseId);
    }

    @PostMapping("/add")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void addTestCase(@RequestBody TestCaseWithBLOBs testCase) {
        testCaseService.addTestCase(testCase);
    }

    @PostMapping("/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void editTestCase(@RequestBody TestCaseWithBLOBs testCase) {
        testCaseService.editTestCase(testCase);
    }

    @PostMapping("/delete/{testCaseId}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int deleteTestCase(@PathVariable String testCaseId) {
        return testCaseService.deleteTestCase(testCaseId);
    }

    @PostMapping("/import/{projectId}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ExcelResponse testCaseImport(MultipartFile file, @PathVariable String projectId) throws NoSuchFieldException {
        return testCaseService.testCaseImport(file, projectId);
    }

    @GetMapping("/export/template")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void testCaseTemplateExport(HttpServletResponse response) {
        testCaseService.testCaseTemplateExport(response);
    }

    @PostMapping("/export/testcase")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void testCaseExport(HttpServletResponse response, @RequestBody TestCaseBatchRequest request) {
        testCaseService.testCaseExport(response, request);
    }

    @PostMapping("/batch/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void editTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.editTestCaseBath(request);
    }

    @PostMapping("/batch/delete")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void deleteTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteTestCaseBath(request);
    }

}
