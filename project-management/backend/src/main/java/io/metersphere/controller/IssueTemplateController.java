package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.IssueTemplateCopyDTO;
import io.metersphere.dto.IssueTemplateDao;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.CopyIssueTemplateRequest;
import io.metersphere.request.UpdateIssueTemplateRequest;
import io.metersphere.service.IssueTemplateService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/field/template/issue")
@RestController

public class IssueTemplateController {
    @Resource
    private IssueTemplateService issueTemplateService;

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_ISSUE, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request)", msClass = IssueTemplateService.class)
    public void add(@RequestBody UpdateIssueTemplateRequest request) {
        issueTemplateService.add(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<IssueTemplate>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<List<IssueTemplate>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, issueTemplateService.list(request));
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.PROJECT_TEMPLATE_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = IssueTemplateService.class)
    public void delete(@PathVariable(value = "id") String id) {
        issueTemplateService.delete(id);
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.PROJECT_TEMPLATE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id, #request.customFields)", content = "#msClass.getLogDetails(#request)", msClass = IssueTemplateService.class)
    public void update(@RequestBody UpdateIssueTemplateRequest request) {
        issueTemplateService.update(request);
    }

    @GetMapping({"/option/{projectId}", "/option"})
    public List<IssueTemplate> list(@PathVariable(required = false) String projectId) {
        return issueTemplateService.getOption(projectId);
    }

    @GetMapping("/get/relate/{projectId}")
    public IssueTemplateDao getTemplate(@PathVariable String projectId) {
        return issueTemplateService.getTemplate(projectId);
    }

    @GetMapping("/get/copy/project/{userId}/{workspaceId}")
    public IssueTemplateCopyDTO getCopyProject(@PathVariable String userId, @PathVariable String workspaceId) {
        return issueTemplateService.getCopyProject(userId, workspaceId);
    }

    @PostMapping("/copy")
    @MsRequestLog(module = OperLogModule.PROJECT_TEMPLATE_MANAGEMENT)
    public void copy(@RequestBody CopyIssueTemplateRequest request) {
        List<IssueTemplate> copyRecords = issueTemplateService.copy(request);
        // 目标项目操作日志
        if (CollectionUtils.isNotEmpty(copyRecords)) {
            copyRecords.forEach(copyRecord -> {
                issueTemplateService.copyIssueTemplateLog(copyRecord.getProjectId(), copyRecord.getName());
            });
        }
    }
}
