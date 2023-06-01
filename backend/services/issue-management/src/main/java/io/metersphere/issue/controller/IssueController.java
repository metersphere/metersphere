package io.metersphere.issue.controller;

import io.metersphere.issue.domain.Issue;
import io.metersphere.issue.service.IssueService;
import io.metersphere.validation.groups.Created;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-5-17
 */
@Api(tags = "缺陷")
@RestController
@RequestMapping("/issue")
public class IssueController {
    @Resource
    private IssueService issueService;

    @GetMapping("/list-all")
    public List<Issue> listAll() {
        return issueService.list();
    }

    @GetMapping("/get/{id}")
    public Issue get(@PathVariable String id) {
        return issueService.get(id);
    }

    @PostMapping("/add")
    public Issue add(@Validated({Created.class}) @RequestBody Issue issue) {
        return issueService.add(issue);
    }

    @PostMapping("/update")
    public Issue update(@Validated({Created.class}) @RequestBody Issue issue) {
        return issueService.update(issue);
    }

    @GetMapping("/delete/{id}")
    public int delete(@PathVariable String id) {
        return issueService.delete(id);
    }
}
