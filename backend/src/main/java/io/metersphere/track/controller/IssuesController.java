package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.ZentaoBuild;
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

    @PostMapping("/add")
    public void addIssues(@RequestBody IssuesUpdateRequest issuesRequest) {
        issuesService.addIssues(issuesRequest);
    }

    @PostMapping("/update")
    public void updateIssues(@RequestBody IssuesUpdateRequest issuesRequest) {
        issuesService.updateIssues(issuesRequest);
    }

    @GetMapping("/get/{id}")
    public List<IssuesDao> getIssues(@PathVariable String id) {
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

    @PostMapping("/delete")
    public void deleteIssue(@RequestBody IssuesRequest request) {
        issuesService.deleteIssue(request);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        issuesService.delete(id);
    }

    @GetMapping("/tapd/user/{caseId}")
    public List<PlatformUser> getTapdUsers(@PathVariable String caseId) {
        return issuesService.getTapdProjectUsers(caseId);
    }

    @GetMapping("/zentao/user/{caseId}")
    public List<PlatformUser> getZentaoUsers(@PathVariable String caseId) {
        return issuesService.getZentaoUsers(caseId);
    }

    @GetMapping("/zentao/builds/{caseId}")
    public List<ZentaoBuild> getZentaoBuilds(@PathVariable String caseId) {
        return issuesService.getZentaoBuilds(caseId);
    }


}
