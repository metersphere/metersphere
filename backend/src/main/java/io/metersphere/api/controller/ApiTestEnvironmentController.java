package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.EnvironmentRequest;
import io.metersphere.service.CheckPermissionService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/environment")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ApiTestEnvironmentController {

    @Resource
    ApiTestEnvironmentService apiTestEnvironmentService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}")
    public List<ApiTestEnvironmentWithBLOBs> list(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return apiTestEnvironmentService.list(projectId);
    }

    /**
     * 查询指定项目和指定名称的环境
     * @param goPage
     * @param pageSize
     * @param environmentRequest
     * @return
     */
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestEnvironmentWithBLOBs>> listByCondition(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentRequest environmentRequest) {
        List<String> projectIds = environmentRequest.getProjectIds();
        for (String projectId : projectIds) {
            checkPermissionService.checkProjectOwner(projectId);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestEnvironmentService.listByConditions(environmentRequest));
    }

    @GetMapping("/get/{id}")
    public ApiTestEnvironmentWithBLOBs get(@PathVariable String id) {
        return apiTestEnvironmentService.get(id);
    }

    @PostMapping("/add")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public String add(@RequestBody ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs) {
        return apiTestEnvironmentService.add(apiTestEnvironmentWithBLOBs);
    }

    @PostMapping(value = "/update")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestBody ApiTestEnvironmentWithBLOBs apiTestEnvironment) {
        apiTestEnvironmentService.update(apiTestEnvironment);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        apiTestEnvironmentService.delete(id);
    }

}
