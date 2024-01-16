package io.metersphere.api.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/scenario")
@Tag(name = "接口测试-接口场景管理")
public class ApiScenarioController {
    @Resource
    private ApiScenarioService apiScenarioService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request));
    }

    @PostMapping("/trash/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getTrashPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        request.setDeleted(true);
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request));
    }

    @PostMapping("/batch/edit")
    @Operation(summary = "接口测试-接口场景管理-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_scenario")
    public void batchUpdate(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
        apiScenarioService.batchEdit(request, SessionUtils.getUserId());
    }

    @GetMapping("follow/{id}")
    @Operation(summary = "接口测试-接口场景管理-关注/ 取消关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public void follow(@PathVariable String id) {
        apiScenarioService.follow(id, SessionUtils.getUserId());
    }

}
