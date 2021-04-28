package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.service.FileService;
import io.metersphere.service.ProjectService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/file")
public class FileController {
    @Resource
    private FileService fileService;
    @Resource
    ProjectService projectService;

    @PostMapping("/listAll/{goPage}/{pageSize}")
    public Pager<List<FileMetadata>> listAll(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCustomFieldRequest request) { //  获取当前工作空间下的所有文件
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        List<ProjectDTO> projectList = projectService.getProjectList(projectRequest);
        List<String> projectIds = new ArrayList<>();
        for(ProjectDTO item : projectList) {
            projectIds.add(item.getId());
        }   //  获得当前工作空间下的所有 project 的 ids
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return PageUtils.setPageInfo(page, fileService.selectFileByWorkSpace(request, projectIds));
    }

    @PostMapping("/delete")
    public void deleteFileByIds(List<String> ids) {
        fileService.deleteFileByIds(ids);
    }

    @PostMapping(value = "/upload/{projectId}", consumes = {"multipart/form-data"})
    public void uploadFile(@PathVariable String projectId, @RequestPart(value = "file") MultipartFile file) {
        if (file != null) {
            fileService.saveFile(file, projectId);
        }
    }

    @PostMapping("/search/{goPage}/{pageSize}")
    public Pager<List<FileMetadata>> searchList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody FileMetadata file) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, fileService.searchList(file, false));
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestPart("request") FileMetadata request, @RequestPart(value = "file", required = false) MultipartFile file) {
        fileService.updateFile(request, file);
    }

    @GetMapping("delete/{fileId}")
    public void deleteFile(@PathVariable String fileId) {
        fileService.deleteFileById(fileId);
    }
}
