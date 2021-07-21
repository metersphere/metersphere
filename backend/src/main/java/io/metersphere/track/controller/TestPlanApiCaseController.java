package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/api/case")
@RestController
public class TestPlanApiCaseController {

    @Resource
    TestPlanApiCaseService testPlanApiCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanApiCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanApiCaseService.list(request));
    }

    @PostMapping("/selectAllTableRows")
    public List<TestPlanApiCaseDTO> selectAllTableRows(@RequestBody TestPlanApiCaseBatchRequest request) {
        return testPlanApiCaseService.selectAllTableRows(request);
    }


    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestCaseDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        return testPlanApiCaseService.relevanceList(goPage, pageSize, request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanApiCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanApiCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanApiCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanApiCaseService.deleteApiCaseBath(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanApiCaseService.class)
    public void batchUpdateEnv(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanApiCaseService.batchUpdateEnv(request);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planIds)", msClass = TestPlanApiCaseService.class)
    public String run(@RequestBody BatchRunDefinitionRequest request) {
        return testPlanApiCaseService.run(request);
    }
}
