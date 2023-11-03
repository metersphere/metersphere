package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.functional.request.FunctionalCaseFollowerRequest;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalCaseNoticeService;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-功能用例")
@RestController
@RequestMapping("/functional/case")
public class FunctionalCaseController {

    @Resource
    private FunctionalCaseService functionalCaseService;

    @Resource
    private ProjectTemplateService projectTemplateService;

    //TODO 获取模板列表(多模板功能暂时不做)

    //TODO 附件操作：文件删除/文件下载/文件预览/文件转存/文件更新

    @GetMapping("/default/template/field/{projectId}")
    @Operation(summary = "功能用例-获取默认模板自定义字段")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    public TemplateDTO getDefaultTemplateField(@PathVariable String projectId) {
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.FUNCTIONAL.name());
        return defaultTemplateDTO;
    }


    @PostMapping("/add")
    @Operation(summary = "功能用例-新增用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addFunctionalCaseLog(#request, #files)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getMainFunctionalCaseDTO(#request.name, #request.caseEditType, #request.projectId, #request.customsFields)", targetClass = FunctionalCaseNoticeService.class)
    public FunctionalCase addFunctionalCase(@Validated @RequestPart("request") FunctionalCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getUserId();
        return functionalCaseService.addFunctionalCase(request, files, userId);
    }


    @GetMapping("/detail/{id}")
    @Operation(summary = "功能用例-查看用例详情")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(@PathVariable String id) {
        return functionalCaseService.getFunctionalCaseDetail(id);
    }


    @PostMapping("/update")
    @Operation(summary = "功能用例-更新用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateFunctionalCaseLog(#request, #files)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainFunctionalCaseDTO(#request.name, #request.caseEditType, #request.projectId, #request.customsFields)", targetClass = FunctionalCaseNoticeService.class)
    public FunctionalCase updateFunctionalCase(@Validated @RequestPart("request") FunctionalCaseEditRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getUserId();
        return functionalCaseService.updateFunctionalCase(request, files, userId);
    }


    @PostMapping("/edit/follower")
    @Operation(summary = "功能用例-关注/取消关注用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void editFollower(@Validated @RequestBody FunctionalCaseFollowerRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.editFollower(request.getFunctionalCaseId(), userId);
    }


    @GetMapping("/follower/{id}")
    @Operation(summary = "功能用例-获取用例关注人")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<String> getFollower(@PathVariable @NotBlank(message = "{functional_case.id.not_blank}") String id) {
        return functionalCaseService.getFollower(id);
    }


    @GetMapping("/version/{id}")
    @Operation(summary = "功能用例-版本信息(用例是否存在多版本)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<FunctionalCaseVersionDTO> getVersion(@PathVariable @NotBlank(message = "{functional_case.id.not_blank}") String id) {
        return functionalCaseService.getFunctionalCaseVersion(id);
    }


    @PostMapping("/delete")
    @Operation(summary = "功能用例-删除用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getDeleteFunctionalCaseDTO(#request.id)", targetClass = FunctionalCaseNoticeService.class)
    public void deleteFunctionalCase(@Validated @RequestBody FunctionalCaseDeleteRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.deleteFunctionalCase(request, userId);
    }

}
