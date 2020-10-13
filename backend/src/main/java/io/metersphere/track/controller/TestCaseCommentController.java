package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import io.metersphere.track.service.TestCaseCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/case/comment")
@RestController
public class TestCaseCommentController {

    @Resource
    TestCaseCommentService testCaseCommentService;

    @PostMapping("/save")
    public void saveComment(@RequestBody SaveCommentRequest request) {
        testCaseCommentService.saveComment(request);
    }

    @GetMapping("/list/{caseId}")
    public List<TestCaseComment> getComments(@PathVariable String caseId) {
        return testCaseCommentService.getComments(caseId);
    }
}
