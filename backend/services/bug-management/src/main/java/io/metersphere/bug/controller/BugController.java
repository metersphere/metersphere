package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.request.BugRequest;
import io.metersphere.bug.service.BugService;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
    public Pager<List<BugDTO>> page(@RequestBody BugRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        request.setUseTrash(false);
        return PageUtils.setPageInfo(page, bugService.list(request));
    }

    @PostMapping("/add")
    @Operation(summary = "缺陷管理-创建缺陷")
    public void add(@Validated({Created.class}) @RequestBody BugEditRequest request, List<MultipartFile> files) {
        bugService.add(request, files, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "缺陷管理-更新缺陷")
    public void update(@Validated({Updated.class}) @RequestBody BugEditRequest request, List<MultipartFile> files) {
        bugService.update(request, files, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "缺陷管理-删除缺陷")
    public void delete(@PathVariable String id) {
        bugService.delete(id);
    }

    @GetMapping("/template/option")
    @Operation(summary = "缺陷管理-获取当前项目缺陷模板选项")
    public List<ProjectTemplateOptionDTO> getTemplateOption() {
        return projectTemplateService.getOption(SessionUtils.getCurrentProjectId(), TemplateScene.BUG.name());
    }

    @GetMapping("/template/{id}")
    @Operation(summary = "缺陷管理-获取模板内容")
    public TemplateDTO getTemplateField(@PathVariable String id) {
        return bugService.getTemplate(id, SessionUtils.getCurrentProjectId());
    }
}
