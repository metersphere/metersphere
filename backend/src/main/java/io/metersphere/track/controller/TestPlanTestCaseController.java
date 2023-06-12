package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.request.testplancase.TestPlanFuncCaseBatchRequest;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/test/plan/case")
@RestController
public class TestPlanTestCaseController {

    @Resource
    TestPlanTestCaseService testPlanTestCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanCaseDTO>> getTestPlanCases(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanCaseRequest request) {
        testPlanTestCaseService.wrapQueryTestPlanCaseRequest(request);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanTestCaseService.list(request));
    }

    /*jenkins测试计划下全部用例*/
    @GetMapping("/list/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getTestPlanCaseByPlanId(@PathVariable String planId) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setMethod("auto");
        return testPlanTestCaseService.listByPlanId(request);
    }

    @PostMapping("/list/minder")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> listForMinder(@RequestBody QueryTestPlanCaseRequest request) {
        return testPlanTestCaseService.listForMinder(request);
    }

    @PostMapping("/list/minder/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanCaseDTO>> listForMinder(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanTestCaseService.listForMinder(request));
    }

    @GetMapping("/list/node/{planId}/{nodePaths}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getTestPlanCasesByNodePath(@PathVariable String planId, @PathVariable String nodePaths) {
        String nodePath = nodePaths.replace("f", "/");
        String[] array = nodePath.split(",");
        List<String> list = Arrays.asList(array);
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setNodePaths(list);
        request.setMethod("auto");
        return testPlanTestCaseService.listByNode(request);
    }

    @GetMapping("/list/node/all/{planId}/{nodePaths}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getTestPlanCasesByNodePaths(@PathVariable String planId, @PathVariable String nodePaths) {
        String nodePath = nodePaths.replace("f", "");
        String[] array = nodePath.split(",");
        List<String> list = Arrays.asList(array);
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setNodePaths(list);
        request.setMethod("auto");
        return testPlanTestCaseService.listByNodes(request);
    }

    @GetMapping("/get/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestPlanCaseDTO getTestPlanCases(@PathVariable String caseId) {
        return testPlanTestCaseService.get(caseId);
    }

    @PostMapping("recent/{count}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getRecentTestCases(@PathVariable int count, @RequestBody QueryTestPlanCaseRequest request) {
        return testPlanTestCaseService.getRecentTestCases(request, count);
    }

    @PostMapping("pending/{count}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getPrepareTestCases(@PathVariable int count, @RequestBody QueryTestPlanCaseRequest request) {
        return testPlanTestCaseService.getPendingTestCases(request, count);
    }

    @PostMapping("/list/all")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getTestPlanCases(@RequestBody QueryTestPlanCaseRequest request) {
        testPlanTestCaseService.wrapQueryTestPlanCaseRequest(request);
        return testPlanTestCaseService.list(request);
    }

    @PostMapping("/idList/all")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<String> getTestPlanCases(@RequestBody TestPlanFuncCaseBatchRequest request) {
        return testPlanTestCaseService.idList(request);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#testPlanTestCase.id)", msClass = TestPlanTestCaseService.class)
    public void editTestCase(@RequestBody TestPlanTestCaseWithBLOBs testPlanTestCase) {
        testPlanTestCaseService.editTestCase(testPlanTestCase);
    }

    @PostMapping("/minder/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getCaseLogDetails(#testPlanTestCases)", msClass = TestPlanTestCaseService.class)
    public void editTestCaseForMinder(@RequestBody List<TestPlanTestCaseWithBLOBs> testPlanTestCases) {
        testPlanTestCaseService.editTestCaseForMinder(testPlanTestCases);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanTestCaseService.class)
    public void editTestCaseBath(@RequestBody TestPlanCaseBatchRequest request) {
        testPlanTestCaseService.editTestCaseBath(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanTestCaseService.class)
    public void deleteTestCaseBath(@RequestBody TestPlanCaseBatchRequest request) {
        testPlanTestCaseService.deleteTestCaseBath(request);
    }

    @PostMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanTestCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanTestCaseService.deleteTestCase(id);
    }

    @GetMapping("/list/failure/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getFailureCases(@PathVariable String planId) {
        return testPlanTestCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanCaseDTO> getAllCases(@PathVariable String planId) {
        return testPlanTestCaseService.getAllCases(planId);
    }

    @PostMapping("/edit/order")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanTestCaseService.updateOrder(request);
    }
}
