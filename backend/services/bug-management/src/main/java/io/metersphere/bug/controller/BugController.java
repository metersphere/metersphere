package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.service.BugService;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Tag(name = "缺陷管理")
@RestController
@RequestMapping("/bug")
public class BugController {

    @Resource
    private BugService bugService;
    @Resource
    private ProjectTemplateService projectTemplateService;

    @PostMapping("/page")
    @Operation(summary = "缺陷管理-获取缺陷列表")
    @RequiresPermissions(PermissionConstants.BUG_READ)
    public Pager<List<BugDTO>> page(@Validated @RequestBody BugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        request.setUseTrash(false);
        return PageUtils.setPageInfo(page, bugService.list(request));
    }

    @PostMapping("/add")
    @Operation(summary = "缺陷管理-创建缺陷")
    @RequiresPermissions(PermissionConstants.BUG_ADD)
    public void add(@Validated({Created.class}) @RequestPart(value = "request") BugEditRequest request,
                    @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        bugService.add(request, files, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "缺陷管理-更新缺陷")
    @RequiresPermissions(PermissionConstants.BUG_UPDATE)
    public void update(@Validated({Updated.class}) @RequestPart(value = "request") BugEditRequest request,
                       @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        bugService.update(request, files, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "缺陷管理-删除缺陷")
    @RequiresPermissions(PermissionConstants.BUG_DELETE)
    public void delete(@PathVariable String id) {
        bugService.delete(id);
    }

    @GetMapping("/template/option")
    @Operation(summary = "缺陷管理-获取当前项目缺陷模板选项")
    @RequiresPermissions(PermissionConstants.BUG_READ)
    public List<ProjectTemplateOptionDTO> getTemplateOption(@RequestParam(value = "projectId") String projectId) {
        return projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
    }

    @GetMapping("/template/{id}")
    @Operation(summary = "缺陷管理-获取模板内容")
    @RequiresPermissions(PermissionConstants.BUG_READ)
    public TemplateDTO getTemplateField(@PathVariable String id, @RequestParam(value = "projectId") String projectId) {
        return bugService.getTemplate(id, projectId);
    }
}
