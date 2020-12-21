package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/test/plan/api/case")
@RestController
public class TestPlanApiCaseController {

    @Resource
    TestPlanApiCaseService testPlanApiCaseService;

//    @PostMapping("/list/{goPage}/{pageSize}")
//    public Pager<List<TestPlanCaseDTO>> getTestPlanCases(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanCaseRequest request) {
//        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
//        return PageUtils.setPageInfo(page, testPlanTestCaseService.list(request));
//    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanApiCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanApiCaseService.list(request));
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestCaseDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, testPlanApiCaseService.relevanceList(request));
    }

    @GetMapping("/delete/{planId}/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int deleteTestCase(@PathVariable String planId, @PathVariable String id) {
        return testPlanApiCaseService.delete(planId, id);
    }

    @PostMapping("/batch/delete")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void deleteApiCaseBath(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanApiCaseService.deleteApiCaseBath(request);
    }


//    @GetMapping("/list/node/all/{planId}/{nodePaths}")
//    public List<TestPlanCaseDTO> getTestPlanCasesByNodePaths(@PathVariable String planId, @PathVariable String nodePaths) {
//        String nodePath = nodePaths.replace("f", "");
//        String[] array = nodePath.split(",");
//        List<String> list = Arrays.asList(array);
//        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
//        request.setPlanId(planId);
//        request.setNodePaths(list);
//        request.setMethod("auto");
//        return testPlanTestCaseService.listByNodes(request);
//    }
//
//    @GetMapping("/get/{caseId}")
//    public TestPlanCaseDTO getTestPlanCases(@PathVariable String caseId) {
//        return testPlanTestCaseService.get(caseId);
//    }
//
//    @PostMapping("recent/{count}")
//    public List<TestPlanCaseDTO> getRecentTestCases(@PathVariable int count, @RequestBody QueryTestPlanCaseRequest request) {
//        return testPlanTestCaseService.getRecentTestCases(request, count);
//    }
//
//    @PostMapping("pending/{count}")
//    public List<TestPlanCaseDTO> getPrepareTestCases(@PathVariable int count, @RequestBody QueryTestPlanCaseRequest request) {
//        return testPlanTestCaseService.getPendingTestCases(request, count);
//    }
//
//    @PostMapping("/list/all")
//    public List<TestPlanCaseDTO> getTestPlanCases(@RequestBody QueryTestPlanCaseRequest request) {
//        return testPlanTestCaseService.list(request);
//    }
//
//    @PostMapping("/list/ids")
//    public List<TestPlanCaseDTO> getTestPlanCaseIds(@RequestBody QueryTestPlanCaseRequest request) {
//        return testPlanTestCaseService.list(request);
//    }
//
//    @PostMapping("/edit")
//    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
//    public void editTestCase(@RequestBody TestPlanTestCaseWithBLOBs testPlanTestCase) {
//        testPlanTestCaseService.editTestCase(testPlanTestCase);
//    }
//
//    @PostMapping("/batch/edit")
//    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
//    public void editTestCaseBath(@RequestBody TestPlanCaseBatchRequest request) {
//        testPlanTestCaseService.editTestCaseBath(request);
//    }
//
//    @PostMapping("/batch/delete")
//    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
//    public void deleteTestCaseBath(@RequestBody TestPlanCaseBatchRequest request) {
//        testPlanTestCaseService.deleteTestCaseBath(request);
//    }
//


}
