package io.metersphere.controller;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.ProjectRequest;
import io.metersphere.service.BaseProjectService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/project")
public class BaseProjectController {
    @Resource
    private BaseProjectService baseProjectService;

    /**
     * 在工作空间下与用户有关的项目
     *
     * @param request userId
     * @return List<ProjectDTO>
     */
    @PostMapping("/list/related")
    public List<Project> getUserProject(@RequestBody ProjectRequest request) {
        // 仅支持查询当前用户的项目
        request.setUserId(SessionUtils.getUserId());
        return baseProjectService.getUserProject(request);
    }

    @PostMapping(value = "upload/files/{projectId}", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.IMPORT, content = "#msClass.getLogDetails(#projectId)", msClass = BaseProjectService.class)
    public List<FileMetadata> uploadFiles(@PathVariable String projectId, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return baseProjectService.uploadFiles(projectId, files);
    }

    @GetMapping("/get/{id}")
    public Project getProject(@PathVariable String id) {
        return baseProjectService.getProjectById(id);
    }

    @GetMapping("/listAll/{workspaceId}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ)
    public List<ProjectDTO> listAll(@PathVariable String workspaceId) {
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(workspaceId);
        return baseProjectService.getProjectList(request);
    }

    @GetMapping(value = "delete/file/{fileId}")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#fileId)", msClass = BaseProjectService.class)
    public void deleteFile(@PathVariable String fileId) {
        baseProjectService.deleteFile(fileId);
    }

    @PostMapping(value = "/update/file/{fileId}", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.IMPORT, content = "#msClass.getLogDetails(#fileId)", msClass = BaseProjectService.class)
    public FileMetadata updateFile(@PathVariable String fileId, @RequestPart(value = "file", required = false) MultipartFile file) {
        return baseProjectService.updateFile(fileId, file);
    }
}
