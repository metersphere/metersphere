package io.metersphere.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.TestCaseDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.issues.IssuesRelevanceRequest;
import io.metersphere.service.TestCaseIssueService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("test/case/issues")
@RestController
public class TestCaseIssuesController {

    @Resource
    private TestCaseIssueService testCaseIssueService;

    @PostMapping("/list")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ, PermissionConstants.PROJECT_TRACK_ISSUE_READ}, logical = Logical.OR)
    public List<TestCaseDTO> list(@RequestBody IssuesRelevanceRequest request) {
        return testCaseIssueService.list(request);
    }

    @PostMapping("/relate")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT}, logical = Logical.AND)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.ASSOCIATE_ISSUE, content = "#msClass.getLogDetails(#request)", msClass = TestCaseIssueService.class)
    public void relate(@RequestBody IssuesRelevanceRequest request) {
        testCaseIssueService.relate(request);
    }
}
