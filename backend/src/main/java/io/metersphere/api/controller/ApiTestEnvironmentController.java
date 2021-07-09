package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiTestEnvironmentDTO;
import io.metersphere.api.dto.ssl.KeyStoreEntry;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.CommandService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.EnvironmentRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/environment")
public class ApiTestEnvironmentController {

    @Resource
    ApiTestEnvironmentService apiTestEnvironmentService;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private CommandService commandService;

    @GetMapping("/list/{projectId}")
    public List<ApiTestEnvironmentWithBLOBs> list(@PathVariable String projectId) {
//        checkPermissionService.checkProjectOwner(projectId);
        return apiTestEnvironmentService.list(projectId);
    }

    /**
     * 查询指定项目和指定名称的环境
     *
     * @param goPage
     * @param pageSize
     * @param environmentRequest
     * @return
     */
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestEnvironmentWithBLOBs>> listByCondition(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentRequest environmentRequest) {
//        List<String> projectIds = environmentRequest.getProjectIds();
//        for (String projectId : projectIds) {
//            checkPermissionService.checkProjectOwner(projectId);
//        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestEnvironmentService.listByConditions(environmentRequest));
    }

    @GetMapping("/get/{id}")
    public ApiTestEnvironmentWithBLOBs get(@PathVariable String id) {
        return apiTestEnvironmentService.get(id);
    }


    @PostMapping(value = "/get/entry")
    public List<KeyStoreEntry> getEntry(@RequestPart("request") String password, @RequestPart(value = "file") MultipartFile sslFiles) {
        return commandService.get(password, sslFiles);
    }

    @PostMapping("/add")
    @MsAuditLog(module = "project_environment_setting", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#apiTestEnvironmentWithBLOBs.id)", msClass = ApiTestEnvironmentService.class)
    public String create(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironmentWithBLOBs, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        return apiTestEnvironmentService.add(apiTestEnvironmentWithBLOBs, sslFiles);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = "project_environment_setting", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#apiTestEnvironment.id)", content = "#msClass.getLogDetails(#apiTestEnvironment.id)", msClass = ApiTestEnvironmentService.class)
    public void update(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironment, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        apiTestEnvironmentService.update(apiTestEnvironment, sslFiles);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = "project_environment_setting", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestEnvironmentService.class)
    public void delete(@PathVariable String id) {
        apiTestEnvironmentService.delete(id);
    }

}
