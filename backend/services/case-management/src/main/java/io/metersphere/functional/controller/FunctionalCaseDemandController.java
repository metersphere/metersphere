package io.metersphere.functional.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.request.FunctionalCaseDemandBatchRequest;
import io.metersphere.functional.request.FunctionalCaseDemandRequest;
import io.metersphere.functional.request.FunctionalThirdDemandPageRequest;
import io.metersphere.functional.request.QueryDemandListRequest;
import io.metersphere.functional.service.FunctionalCaseDemandService;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.plugin.platform.dto.response.PlatformDemandDTO;
import io.metersphere.plugin.platform.utils.PluginPager;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-功能用例-关联需求")
@RestController
@RequestMapping("/functional/case/demand")
public class FunctionalCaseDemandController {

    @Resource
    private FunctionalCaseDemandService functionalCaseDemandService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-关联需求-获取已关联的需求列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public Pager<List<FunctionalDemandDTO>> listFunctionalCaseDemands(@Validated @RequestBody QueryDemandListRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, functionalCaseDemandService.listFunctionalCaseDemands(request));
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例详情-关联需求-新增/关联需求")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public void addDemand(@RequestBody @Validated FunctionalCaseDemandRequest request) {
        functionalCaseDemandService.addDemand(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "用例管理-功能用例-关联需求-更新需求")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public void updateDemand(@RequestBody @Validated({Updated.class}) FunctionalCaseDemandRequest request) {
        functionalCaseDemandService.updateDemand(request, SessionUtils.getUserId());
    }

    @GetMapping("/cancel/{id}")
    @Operation(summary = "用例管理-功能用例-关联需求-取消关联需求")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateLog(#id)", msClass = FunctionalCaseLogService.class)
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public void deleteDemand(@PathVariable @NotBlank(message = "{functional_case.id.not_blank}") String id) {
        functionalCaseDemandService.deleteDemand(id);
    }

    @PostMapping("/batch/relevance")
    @Operation(summary = "用例管理-功能用例列表-关联需求-批量关联新增需求")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public void batchRelevance(@RequestBody @Validated FunctionalCaseDemandBatchRequest request) {
        functionalCaseDemandService.batchRelevance(request, SessionUtils.getUserId());
    }

    @PostMapping("/third/list/page")
    @Operation(summary = "用例管理-功能用例-关联需求-获取三方需求列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public PluginPager<PlatformDemandDTO> pageDemand(@RequestBody @Validated FunctionalThirdDemandPageRequest request) {
       return functionalCaseDemandService.pageDemand(request);
    }
}
