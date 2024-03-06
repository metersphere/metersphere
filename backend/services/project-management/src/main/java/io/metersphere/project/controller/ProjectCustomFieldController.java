package io.metersphere.project.controller;


import io.metersphere.project.service.ProjectCustomFieldLogService;
import io.metersphere.project.service.ProjectCustomFieldService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jianxing
 */
@Tag(name = "项目管理-自定义字段")
@RestController
@RequestMapping("/project/custom/field")
public class ProjectCustomFieldController {

    @Resource
    private ProjectCustomFieldService projectCustomFieldService;

    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取自定义字段列表")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<CustomFieldDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                  @PathVariable String projectId,
                                  @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                  @PathVariable String scene) {
        return projectCustomFieldService.list(projectId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取自定义字段详情")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public CustomFieldDTO get(@PathVariable String id) {
        return projectCustomFieldService.getCustomFieldDTOWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ProjectCustomFieldLogService.class)
    public CustomField add(@Validated({Created.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyBean(customField, request);
        customField.setCreateUser(SessionUtils.getUserId());
        return projectCustomFieldService.add(customField, request.getOptions());
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectCustomFieldLogService.class)
    public CustomField update(@Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyBean(customField, request);
        return projectCustomFieldService.update(customField, request.getOptions());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ProjectCustomFieldLogService.class)
    public void delete(@PathVariable String id) {
        projectCustomFieldService.delete(id);
    }
}
