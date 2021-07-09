package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.zentao.ZentaoBuild;
import io.metersphere.track.request.testcase.AuthUserIssueRequest;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import io.metersphere.track.service.IssuesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("issues")
@RestController
public class IssuesController {

    @Resource
    private IssuesService issuesService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<IssuesDao>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody IssuesRequest request) {
        Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, issuesService.list(request));
    }

    @PostMapping("/list/relate/{goPage}/{pageSize}")
    public Pager<List<IssuesDao>> relateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody IssuesRequest request) {
        Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, issuesService.relateList(request));
    }

    @PostMapping("/add")
    @MsAuditLog(module = "track_bug", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#issuesRequest)", msClass = IssuesService.class)
    public void addIssues(@RequestBody IssuesUpdateRequest issuesRequest) {
        issuesService.addIssues(issuesRequest);
    }

    @PostMapping("/update")
    @MsAuditLog(module = "track_bug", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#issuesRequest.id)", content = "#msClass.getLogDetails(#issuesRequest.id)", msClass = IssuesService.class)
    public void updateIssues(@RequestBody IssuesUpdateRequest issuesRequest) {
        issuesService.updateIssues(issuesRequest);
    }

    @GetMapping("/get/{id}")
    public List<IssuesDao> getIssues(@PathVariable String id) {
        return issuesService.getIssues(id);
    }

    @GetMapping("/auth/{orgId}/{platform}")
    public void testAuth(@PathVariable String orgId, @PathVariable String platform) {
        issuesService.testAuth(orgId, platform);
    }

    @PostMapping("/user/auth")
    public void userAuth(@RequestBody AuthUserIssueRequest authUserIssueRequest) {
        issuesService.userAuth(authUserIssueRequest);
    }

    @GetMapping("/close/{id}")
    public void closeLocalIssue(@PathVariable String id) {
        issuesService.closeLocalIssue(id);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "track_bug", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = IssuesService.class)
    public void deleteIssue(@RequestBody IssuesRequest request) {
        issuesService.deleteIssue(request);
    }

    @PostMapping("/delete/relate")
    public void deleteRelate(@RequestBody IssuesRequest request) {
        issuesService.deleteIssueRelate(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = "track_bug", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = IssuesService.class)
    public void delete(@PathVariable String id) {
        issuesService.delete(id);
    }

    @PostMapping("/tapd/user")
    public List<PlatformUser> getTapdUsers(@RequestBody IssuesRequest request) {
        return issuesService.getTapdProjectUsers(request);
    }

    @PostMapping("/zentao/user")
    public List<PlatformUser> getZentaoUsers(@RequestBody IssuesRequest request) {
        return issuesService.getZentaoUsers(request);
    }

    @PostMapping("/zentao/builds")
    public List<ZentaoBuild> getZentaoBuilds(@RequestBody IssuesRequest request) {
        return issuesService.getZentaoBuilds(request);
    }

    @GetMapping("/sync/{projectId}")
    public void getPlatformIssue(@PathVariable String projectId) {
        issuesService.syncThirdPartyIssues(projectId);
    }
}
