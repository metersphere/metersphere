package io.metersphere.track.controller;

import io.metersphere.base.domain.Issues;
import io.metersphere.track.domain.TapdUser;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("issues")
@RestController
public class TestCaseIssuesController {

    @Resource
    private IssuesService issuesService;

    @PostMapping("/add")
    public void addIssues(@RequestBody IssuesRequest issuesRequest) {
        issuesService.addIssues(issuesRequest);
    }

    @GetMapping("/get/{id}")
    public List<Issues> getIssues(@PathVariable String id) {
        return issuesService.getIssues(id);
    }

    @GetMapping("/auth/{platform}")
    public void testAuth(@PathVariable String platform) {
        issuesService.testAuth(platform);
    }

    @GetMapping("/close/{id}")
    public void closeLocalIssue(@PathVariable String id) {
        issuesService.closeLocalIssue(id);
    }

    @GetMapping("/delete/{id}")
    public void deleteIssue(@PathVariable String id) {
        issuesService.deleteIssue(id);
    }

    @GetMapping("/tapd/user/{caseId}")
    public List<TapdUser> getTapdUsers(@PathVariable String caseId) {
        return issuesService.getTapdProjectUsers(caseId);
    }

}
