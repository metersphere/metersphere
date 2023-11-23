package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.service.definition.ApiDefinitionLogService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @author lan
 */
@RestController
@RequestMapping(value = "/api/definition")
public class ApiDefinitionController {
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-添加接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_ADD)
    // 添加接口Log示例
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDefinitionLogService.class)
    public ApiDefinition add(@Validated @RequestPart("request") ApiDefinitionAddRequest request,
                             @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-更新接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDefinitionLogService.class)
    public ApiDefinition update(@Validated @RequestPart("request") ApiDefinitionUpdateRequest request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.update(request, bodyFiles, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-update")
    @Operation(summary = "接口测试-接口管理-批量更新接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    // 添加修改Log示例
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateLog(#request)", msClass = ApiDefinitionLogService.class)
    public void batchUpdate(@Validated @RequestBody ApiDefinitionBatchUpdateRequest request) {
        apiDefinitionService.batchUpdate(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "接口测试-接口管理-删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#request)", msClass = ApiDefinitionLogService.class)
    public void delete(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.delete(request, SessionUtils.getUserId());
    }
    @PostMapping(value = "/batch-del")
    @Operation(summary = "接口测试-接口管理-批量删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDelLog(#request)", msClass = ApiDefinitionLogService.class)
    public void batchDelete(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/copy")
    @Operation(summary = "接口测试-接口管理-复制接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.copyLog(#request)", msClass = ApiDefinitionLogService.class)
    public ApiDefinition copy(@Validated @RequestBody ApiDefinitionCopyRequest request) {
        return apiDefinitionService.copy(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-move")
    @Operation(summary = "接口测试-接口管理-批量移动接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchMoveLog(#request)", msClass = ApiDefinitionLogService.class)
    public void batchMove(@Validated @RequestBody ApiDefinitionBatchMoveRequest request) {
        apiDefinitionService.batchMove(request, SessionUtils.getUserId());
    }

    @GetMapping("/version/{id}")
    @Operation(summary = "接口测试-接口管理-版本信息(接口是否存在多版本)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public List<ApiDefinitionVersionDTO> getApiDefinitionVersion(@PathVariable @NotBlank(message = "{api_definition.id.not_blank}") String id) {
        return apiDefinitionService.getApiDefinitionVersion(id);
    }

    @GetMapping(value = "/get-detail/{id}")
    @Operation(summary = "接口测试-接口管理-获取接口详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public ApiDefinitionDTO get(@PathVariable String id) {
        return apiDefinitionService.get(id, SessionUtils.getUserId());
    }

    @GetMapping("/follow/{id}")
    @Operation(summary = "接口测试-接口管理-关注/取消关注用例")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.followLog(#id)", msClass = ApiDefinitionLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    public void follow(@PathVariable String id) {
        apiDefinitionService.follow(id, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口管理-接口列表(deleted 状态为 0 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiDefinitionDTO>> getPage(@Validated @RequestBody ApiDefinitionPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getApiDefinitionPage(request));
    }

    @PostMapping(value = "/restore")
    @Operation(summary = "接口测试-接口管理-恢复回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.restoreLog(#request)", msClass = ApiDefinitionLogService.class)
    public void restore(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.restore(request, SessionUtils.getUserId());
    }
    @PostMapping(value = "/recycle-del")
    @Operation(summary = "接口测试-接口管理-删除回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.recycleDelLog(#request)", msClass = ApiDefinitionLogService.class)
    public void recycleDel(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.recycleDel(request, SessionUtils.getUserId());
    }
    @PostMapping(value = "/batch-restore")
    @Operation(summary = "接口测试-接口管理-批量从回收站恢复接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchRestoreLog(#request)", msClass = ApiDefinitionLogService.class)
    public void batchRestore(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchRestore(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-recycle-del")
    @Operation(summary = "接口测试-接口管理-批量从回收站删除接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    public void batchRecycleDel(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchRecycleDel(request, SessionUtils.getUserId());
    }

}
