package io.metersphere.track.controller;

import io.metersphere.track.dto.TestCaseCommentDTO;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import io.metersphere.track.service.TestCaseCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/case/comment")
@RestController
public class TestCaseCommentController {

    @Resource
    private TestCaseCommentService testCaseCommentService;

    @PostMapping("/save")
    public void saveComment(@RequestBody SaveCommentRequest request) {
        testCaseCommentService.saveComment(request);
    }

    @GetMapping("/list/{caseId}")
    public List<TestCaseCommentDTO> getCaseComments(@PathVariable String caseId) {
        return testCaseCommentService.getCaseComments(caseId);
    }

    @GetMapping("/delete/{commentId}")
    public void deleteComment(@PathVariable String commentId) {
        testCaseCommentService.delete(commentId);
    }

    @PostMapping("/edit")
    public void editComment(@RequestBody SaveCommentRequest request) {
        testCaseCommentService.edit(request);
    }
}
