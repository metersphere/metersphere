package io.metersphere.project.controller;

import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.dto.filemanagement.request.FileAssociationDeleteRequest;
import io.metersphere.project.dto.filemanagement.response.FileAssociationResponse;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理-文件管理-文件关联")
@RestController
@RequestMapping("/project/file/association")
public class FileAssociationController {

    @Resource
    private FileAssociationService fileAssociationService;

    @GetMapping("/list/{id}")
    @Operation(summary = "项目管理-文件管理-文件关联-文件资源关联列表")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#id", resourceType = "file_metadata")
    public List<FileAssociationResponse> getAssociationList(@PathVariable String id) {
        return fileAssociationService.selectFileAllVersionAssociation(id);
    }

    @GetMapping("/upgrade/{projectId}/{id}")
    @Operation(summary = "项目管理-文件管理-文件关联-更新资源关联的文件到最新版本")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public String upgrade(@PathVariable String projectId,@PathVariable String id) {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator(SessionUtils.getUserId())
                .projectId(projectId)
                .build();

        return fileAssociationService.upgrade(id,fileLogRecord);
    }

    @PostMapping("/delete")
    @Operation(summary = "项目管理-文件管理-文件关联-取消文件和资源的关联")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public int delete(@RequestBody @Validated FileAssociationDeleteRequest request) {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator(SessionUtils.getUserId())
                .projectId(request.getProjectId())
                .build();

        return fileAssociationService.deleteByIds(request.getAssociationIds(), fileLogRecord);
    }
}
