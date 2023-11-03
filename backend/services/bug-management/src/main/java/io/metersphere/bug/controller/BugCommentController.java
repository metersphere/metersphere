package io.metersphere.bug.controller;

import io.metersphere.bug.domain.BugComment;
import io.metersphere.bug.dto.BugCommentDTO;
import io.metersphere.bug.dto.BugCommentUserInfo;
import io.metersphere.bug.dto.request.BugCommentEditRequest;
import io.metersphere.bug.service.BugCommentService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "缺陷管理-评论")
@RestController
@RequestMapping("/bug/comment")
public class BugCommentController {

    @Resource
    private BugCommentService bugCommentService;

    @GetMapping("/get/{bugId}")
    public List<BugCommentDTO> get(@PathVariable String bugId) {
        return bugCommentService.getComments(bugId);
    }

    @GetMapping("/user-extra/{bugId}")
    public List<BugCommentUserInfo> getUserExtra(@PathVariable String bugId) {
        return bugCommentService.getUserExtra(bugId);
    }

    @PostMapping("/add")
    public BugComment add(@RequestBody BugCommentEditRequest request) {
        return bugCommentService.addComment(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    public BugComment update(@RequestBody BugCommentEditRequest request) {
        return bugCommentService.updateComment(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{commentId}")
    public void delete(@PathVariable String commentId) {
        bugCommentService.deleteComment(commentId);
    }
}
