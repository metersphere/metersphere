package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "功能测试-功能用例")
@RestController
@RequestMapping("/functional/case")
public class FunctionalCaseController {

    @Resource
    private FunctionalCaseService functionalCaseService;

    @Resource
    private ProjectTemplateService projectTemplateService;

    //TODO 获取模板列表(多模板功能暂时不做)

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
    @Log(type = OperationLogType.ADD, expression = "#msClass.addFunctionalCaseLog(#request, #files)", msClass = FunctionalCaseService.class)
    public FunctionalCase addFunctionalCase(@Validated @RequestPart("request") FunctionalCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getUserId();
        return functionalCaseService.addFunctionalCase(request, files, userId);
    }


    @GetMapping("/detail/{functionalCaseId}")
    @Operation(summary = "功能用例-查看用例详情")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(@PathVariable String functionalCaseId) {
        return functionalCaseService.getFunctionalCaseDetail(functionalCaseId);
    }
}
