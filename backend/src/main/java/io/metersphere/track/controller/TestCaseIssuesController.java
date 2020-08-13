package io.metersphere.track.controller;

import io.metersphere.service.IssuesService;
import io.metersphere.track.dto.IssuesDTO;
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
    public List<IssuesDTO> getIssues(@PathVariable String id) {
        return issuesService.getIssues(id);
    }
}
