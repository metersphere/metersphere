package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.FunctionalCaseRelationshipDTO;
import io.metersphere.functional.request.RelationshipAddRequest;
import io.metersphere.functional.request.RelationshipDeleteRequest;
import io.metersphere.functional.request.RelationshipPageRequest;
import io.metersphere.functional.request.RelationshipRequest;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalCaseRelationshipEdgeService;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-功能用例-用例详情-前后置关系")
@RestController
@RequestMapping("/functional/case/relationship")
public class FunctionalCaseRelationshipController {

    @Resource
    private FunctionalCaseService functionalCaseService;
    @Resource
    private FunctionalCaseRelationshipEdgeService functionalCaseRelationshipEdgeService;

    @GetMapping("/get-ids/{caseId}")
    @Operation(summary = "用例管理-功能用例-用例详情-前后置关系-获取已关联用例id集合(关联用例弹窗前调用)")
    @CheckOwner(resourceId = "#caseId", resourceType = "functional_case")
    public List<String> getCaseIds(@PathVariable String caseId) {
        return functionalCaseRelationshipEdgeService.getExcludeIds(caseId);
    }


    @PostMapping("/relate/page")
    @Operation(summary = "用例管理-功能用例-用例详情-前后置关系-弹窗获取用例列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FunctionalCasePageDTO>> getFunctionalCasePage(@Validated @RequestBody RelationshipPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, functionalCaseService.getFunctionalCasePage(request, false, true));
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例-用例详情-前后置关系-添加前后置关系")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void add(@Validated @RequestBody RelationshipAddRequest request) {
        List<String> excludeIds = functionalCaseRelationshipEdgeService.getExcludeIds(request.getId());
        request.getExcludeIds().addAll(excludeIds);
        List<String> ids = functionalCaseService.doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            functionalCaseRelationshipEdgeService.add(request, ids, SessionUtils.getUserId());
        }
    }


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-用例详情-前后置关系-列表查询")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FunctionalCaseRelationshipDTO>> getRelationshipCase(@Validated @RequestBody RelationshipRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalCaseRelationshipEdgeService.getFunctionalCasePage(request));
    }


    @PostMapping("/delete")
    @Operation(summary = "用例管理-功能用例-用例详情-前后置关系-取消关联")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateRelateLog(#request)", msClass = FunctionalCaseLogService.class)
    @CheckOwner(resourceId = "#request.getCaseId", resourceType = "functional_case")
    public void delete(@Validated @RequestBody RelationshipDeleteRequest request) {
        functionalCaseRelationshipEdgeService.delete(request);
    }
}
