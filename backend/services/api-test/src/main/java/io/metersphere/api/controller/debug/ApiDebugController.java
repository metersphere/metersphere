package io.metersphere.api.controller.debug;

import io.metersphere.api.curl.domain.CurlEntity;
import io.metersphere.api.curl.util.CurlParserUtil;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiImportCurlRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.debug.ApiDebugLogService;
import io.metersphere.api.service.debug.ApiDebugService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.file.annotation.FileLimit;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
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
import java.util.Map;
import java.util.Optional;

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
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private ApiFileResourceService apiFileResourceService;

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

    @FileLimit
    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口调试所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEBUG_ADD, PermissionConstants.PROJECT_API_DEBUG_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
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
    public TaskRequestDTO debug(@Validated @RequestBody ApiDebugRunRequest request) {
        return apiDebugService.debug(request);
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "接口调试-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.moveLog(#request.getTargetId())", msClass = ApiDebugLogService.class)
    @CheckOwner(resourceId = "#request.getTargetId()", resourceType = "api_debug")
    public void editPos(@Validated @RequestBody ApiEditPosRequest request) {
        apiDebugService.editPos(request, SessionUtils.getUserId());
    }

    @PostMapping("/transfer")
    @Operation(summary = "接口测试-接口调试-附件-文件转存")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    public String transfer(@Validated @RequestBody ApiTransferRequest request) {
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(request.getProjectId(), request.getSourceId());
        return apiFileResourceService.transfer(request, SessionUtils.getUserId(), apiDebugDir);
    }

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "接口测试-接口调试-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }


    @PostMapping("/import-curl")
    @Operation(summary = "接口测试-接口调试-导入curl")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEBUG_READ, PermissionConstants.PROJECT_API_SCENARIO_ADD}, logical = Logical.OR)
    public CurlEntity importCurl(@RequestBody ApiImportCurlRequest request) {
        CurlEntity parse = CurlParserUtil.parse(request.getCurl());
        Optional.ofNullable(parse.getBody()).ifPresent(body -> {
            if (parse.getMethod() == CurlEntity.Method.GET) {
                Map map = JSON.parseMap(JSON.toFormatJSONString(body));
                parse.getQueryParams().putAll(map);
            }
        });
        return parse;
    }

}