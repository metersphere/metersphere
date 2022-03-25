package io.metersphere.track.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.issues.IssuesRelevanceRequest;
import io.metersphere.track.service.TestCaseIssueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("test/case/issues")
@RestController
public class TestCaseIssuesController {

    @Resource
    private TestCaseIssueService testCaseIssueService;

    @PostMapping("/list")
    public List<TestCaseDTO> list(@RequestBody IssuesRelevanceRequest request) {
        return testCaseIssueService.list(request);
    }

    @PostMapping("/relate")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.ASSOCIATE_ISSUE, content = "#msClass.getLogDetails(#request)", msClass = TestCaseIssueService.class)
    public void relate(@RequestBody IssuesRelevanceRequest request) {
        testCaseIssueService.relate(request);
    }
}
