package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.AddProjectRequest;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.ProjectService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;

    @GetMapping("/listAll")
    public List<ProjectDTO> listAll() {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        return projectService.getProjectList(request);
    }

    /*jenkins项目列表*/
    @GetMapping("/listAll/{workspaceId}")
    public List<ProjectDTO> jlistAll(@PathVariable String workspaceId) {
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(workspaceId);
        return projectService.getProjectList(request);
    }

    @GetMapping("/recent/{count}")
    public List<Project> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return projectService.getRecentProjectList(request);
    }

    @GetMapping("/get/{id}")
    public Project getProject(@PathVariable String id) {
        return projectService.getProjectById(id);
    }
    @GetMapping("/member/size/{id}")
    public long getProjectMemberSize(@PathVariable String id) {
        return projectService.getProjectMemberSize(id);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#project.id)", msClass = ProjectService.class)
    public Project addProject(@RequestBody AddProjectRequest project, HttpServletRequest request) {
        Project returnModel = projectService.addProject(project);
        //创建项目的时候默认增加Mock环境
        apiTestEnvironmentService.getMockEnvironmentByProjectId(returnModel.getId());
        return returnModel;
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ProjectDTO>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, projectService.getProjectList(request));
    }

    /**
     * 在工作空间下与用户有关的项目
     *
     * @param request userId
     * @return List<ProjectDTO>
     */
    @PostMapping("/list/related")
    public List<ProjectDTO> getUserProject(@RequestBody ProjectRequest request) {
        return projectService.getUserProject(request);
    }


    @GetMapping("/delete/{projectId}")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#projectId)", msClass = ProjectService.class)
    public void deleteProject(@PathVariable(value = "projectId") String projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#Project.id)", content = "#msClass.getLogDetails(#Project.id)", msClass = ProjectService.class)
    public void updateProject(@RequestBody AddProjectRequest Project) {
        projectService.updateProject(Project);
    }

    @PostMapping(value = "upload/files/{projectId}", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.IMPORT, content = "#msClass.getLogDetails(#projectId)", msClass = ProjectService.class)
    public List<FileMetadata> uploadFiles(@PathVariable String projectId, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return projectService.uploadFiles(projectId, files);
    }

    @PostMapping(value = "/update/file/{fileId}", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.IMPORT, content = "#msClass.getLogDetails(#fileId)", msClass = ProjectService.class)
    public FileMetadata updateFile(@PathVariable String fileId, @RequestPart(value = "file", required = false) MultipartFile file) {
        return projectService.updateFile(fileId, file);
    }

    @GetMapping(value = "delete/file/{fileId}")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#fileId)", msClass = ProjectService.class)
    public void deleteFile(@PathVariable String fileId) {
        projectService.deleteFile(fileId);
    }

    @PostMapping("/member/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MEMBER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#memberDTO)", content = "#msClass.getLogDetails(#memberDTO)", msClass = ProjectService.class)
    public void updateMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        projectService.updateMember(memberDTO);
    }

    @GetMapping("/getOwnerProjectIds")
    public Collection<String> getOwnerProjectIds() {
        return checkPermissionService.getUserRelatedProjectIds();
    }

    @GetMapping("/getOwnerProjects")
    public List<ProjectDTO>  getOwnerProjects() {
        return checkPermissionService.getOwnerProjects();
    }

    @GetMapping("/genTcpMockPort/{id}")
    public String genTcpMockPort(@PathVariable String id){
        return projectService.genTcpMockPort(id);
    }

    @GetMapping("version/enable/{projectId}")
    public boolean isVersionEnable(@PathVariable String projectId) {
        return projectService.isVersionEnable(projectId);
    }

    @PostMapping("/check/third/project")
    public void checkThirdProjectExist(@RequestBody Project project) {
        projectService.checkThirdProjectExist(project);
    }
}
