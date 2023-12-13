package io.metersphere.api.controller.debug;

import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.service.debug.ApiDebugLogService;
import io.metersphere.api.service.debug.ApiDebugService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-11-6
 */
@RestController
@RequestMapping("/api/debug")
@Tag(name = "接口调试")
public class ApiDebugController {

    @Resource
    private ApiDebugService apiDebugService;

    @GetMapping("/list/{protocol}")
    @Operation(summary = "获取接口调试列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_READ)
    public List<ApiDebugSimpleDTO> list(@PathVariable String protocol) {
        return apiDebugService.list(protocol, SessionUtils.getUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取接口调试详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_READ)
    public ApiDebugDTO get(@PathVariable String id) {
        return apiDebugService.get(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建接口调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDebugLogService.class)
    public ApiDebug add(@Validated @RequestBody ApiDebugAddRequest request) {
        return apiDebugService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口调试所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEBUG_ADD, PermissionConstants.PROJECT_API_DEBUG_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiDebugService.uploadTempFile(file);
    }

    @PostMapping("/update")
    @Operation(summary = "更新接口调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDebugLogService.class)
    public ApiDebug update(@Validated @RequestBody ApiDebugUpdateRequest request) {
        return apiDebugService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除接口调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiDebugLogService.class)
    public void delete(@PathVariable String id) {
        apiDebugService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/debug")
    @Operation(summary = "运行接口调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_EXECUTE)
    public String debug(@Validated @RequestBody ApiDebugRunRequest request) {
        return apiDebugService.debug(request);
    }
}