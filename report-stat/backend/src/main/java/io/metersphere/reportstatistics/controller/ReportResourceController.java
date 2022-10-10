package io.metersphere.reportstatistics.controller;

import io.metersphere.base.domain.User;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.reportstatistics.dto.TreeNodeDTO;
import io.metersphere.reportstatistics.service.remote.track.TestCaseNodeRemoteService;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.service.BaseUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/report/resource")
public class ReportResourceController {

    @Resource
    private BaseUserService baseUserService;
    @Resource
    private TestCaseNodeRemoteService testCaseNodeRemoteService;

    @GetMapping("/test/case/node/{projectId}")
    public List<TreeNodeDTO> getNodeByProjectId(@PathVariable String projectId) {
        return testCaseNodeRemoteService.selectTreeNodesByProjectId(projectId);
    }

    @GetMapping("/project/member/list")
    public List<User> getProjectMemberListAll() {
        QueryMemberRequest request = new QueryMemberRequest();
        request.setProjectId(SessionUtils.getCurrentProjectId());
        return baseUserService.getProjectMemberList(request);
    }
}
