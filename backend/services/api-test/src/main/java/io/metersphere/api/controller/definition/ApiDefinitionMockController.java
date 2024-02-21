package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.constants.ApiResource;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.ApiValidateService;
import io.metersphere.api.service.definition.ApiDefinitionMockLogService;
import io.metersphere.api.service.definition.ApiDefinitionMockNoticeService;
import io.metersphere.api.service.definition.ApiDefinitionMockService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @author lan
 */
@RestController
@RequestMapping(value = "/api/definition/mock")
@Tag(name = "接口测试-接口管理-接口定义-Mock")
public class ApiDefinitionMockController {

    @Resource
    private ApiDefinitionMockService apiDefinitionMockService;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiValidateService apiValidateService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口管理-接口 Mock")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiDefinitionMockDTO>> getPage(@Validated @RequestBody ApiDefinitionMockPageRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionMockService.getPage(request));
    }

    @PostMapping(value = "/detail")
    @Operation(summary = "接口测试-接口管理-获取 Mock 详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_READ)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition_mock")
    public ApiDefinitionMockDTO detail(@Validated @RequestBody ApiDefinitionMockRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiDefinitionMockService.detail(request);
    }

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-添加 Mock")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDefinitionMockLogService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.MOCK_CREATE, target = "#targetClass.getApiMockDTO(#request)", targetClass = ApiDefinitionMockNoticeService.class)
    public ApiDefinitionMock add(@Validated @RequestBody ApiDefinitionMockAddRequest request) {
        apiValidateService.validateApiMenuInProject(request.getApiDefinitionId(), ApiResource.API_DEFINITION.name());
        return apiDefinitionMockService.create(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-更新 Mock")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDefinitionMockLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition_mock")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.MOCK_UPDATE, target = "#targetClass.getApiMockDTO(#request)", targetClass = ApiDefinitionMockNoticeService.class)
    public ApiDefinitionMock update(@Validated @RequestBody ApiDefinitionMockUpdateRequest request) {
        apiValidateService.validateApiMenuInProject(request.getId(), ApiResource.API_DEFINITION_MOCK.name());
        return apiDefinitionMockService.update(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/enable/{id}")
    @Operation(summary = "接口测试-接口管理-更新 Mock-更新状态")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateEnableLog(#id)", msClass = ApiDefinitionMockLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition_mock")
    public void updateEnable(@PathVariable String id) {
        apiValidateService.validateApiMenuInProject(id, ApiResource.API_DEFINITION_MOCK.name());
        apiDefinitionMockService.updateEnable(id);
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "接口测试-接口管理-删除 Mock")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#request)", msClass = ApiDefinitionMockLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition_mock")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.MOCK_DELETE, target = "#targetClass.getApiMockDTO(#request.id)", targetClass = ApiDefinitionMockNoticeService.class)
    public void delete(@Validated @RequestBody ApiDefinitionMockRequest request) {
        apiValidateService.validateApiMenuInProject(request.getId(), ApiResource.API_DEFINITION_MOCK.name());
        apiDefinitionMockService.delete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/copy")
    @Operation(summary = "接口测试-接口管理-复制 Mock")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.copyLog(#request)", msClass = ApiDefinitionMockLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition_mock")
    public ApiDefinitionMock copy(@Validated @RequestBody ApiDefinitionMockRequest request) {
        apiValidateService.validateApiMenuInProject(request.getId(), ApiResource.API_DEFINITION_MOCK.name());
        return apiDefinitionMockService.copy(request, SessionUtils.getUserId());
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口 Mock 所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD, PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }
}
