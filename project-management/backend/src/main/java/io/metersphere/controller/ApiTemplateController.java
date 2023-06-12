package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.ApiTemplateDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.UpdateApiTemplateRequest;
import io.metersphere.service.ApiTemplateService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("field/template/api")
@RestController
public class ApiTemplateController {

    @Resource
    private ApiTemplateService apiTemplateService;

    @PostMapping("/add")
    @RequiresPermissions("PROJECT_TEMPLATE:READ+API_TEMPLATE")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_API, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = ApiTemplateService.class)
    public void add(@RequestBody UpdateApiTemplateRequest request) {
        apiTemplateService.add(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(value = {"PROJECT_TEMPLATE:READ+API_TEMPLATE", "PROJECT_TEMPLATE:READ"}, logical = Logical.OR)
    public Pager<List<ApiTemplate>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<List<ApiTemplate>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTemplateService.list(request));
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions("PROJECT_TEMPLATE:READ+API_TEMPLATE")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_API, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTemplateService.class)
    public void delete(@PathVariable(value = "id") String id) {
        apiTemplateService.delete(id);
    }

    @PostMapping("/update")
    @RequiresPermissions("PROJECT_TEMPLATE:READ+API_TEMPLATE")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_API, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = ApiTemplateService.class)
    public void update(@RequestBody UpdateApiTemplateRequest request) {
        apiTemplateService.update(request);
    }

    @GetMapping({"/option/{projectId}", "/option"})
    @RequiresPermissions(value = {"PROJECT_TEMPLATE:READ+API_TEMPLATE", "PROJECT_TEMPLATE:READ",
            PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_CREATE, PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_EDIT}, logical = Logical.OR)
    public List<ApiTemplate> list(@PathVariable(required = false) String projectId) {
        return apiTemplateService.getOption(projectId);
    }

    @GetMapping("/get-template/relate/{projectId}")
    @RequiresPermissions(value = {"PROJECT_TEMPLATE:READ+API_TEMPLATE", "PROJECT_TEMPLATE:READ",
            PermissionConstants.PROJECT_API_DEFINITION_READ}, logical = Logical.OR)
    public ApiTemplateDTO getTemplate(@PathVariable String projectId) {
        return apiTemplateService.getTemplate(projectId);
    }
}
