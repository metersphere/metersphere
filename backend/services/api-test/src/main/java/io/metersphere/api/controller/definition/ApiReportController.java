package io.metersphere.api.controller.definition;

import com.fasterxml.jackson.databind.node.TextNode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportDTO;
import io.metersphere.api.dto.definition.ApiReportDetailDTO;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.report.ApiReportListDTO;
import io.metersphere.api.service.ApiReportShareService;
import io.metersphere.api.service.definition.ApiReportLogService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/report/case")
@Tag(name = "接口测试-接口报告-用例")
public class ApiReportController {
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiReportShareService apiReportShareService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口报告-用例()")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public Pager<List<ApiReportListDTO>> getPage(@Validated @RequestBody ApiReportPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "start_time desc");
        return PageUtils.setPageInfo(page, apiReportService.getPage(request));
    }

    @PostMapping("/rename/{id}")
    @Operation(summary = "接口测试-接口报告-用例报告重命名")
    @CheckOwner(resourceId = "#id", resourceType = "api_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiReportLogService.class)
    public void rename(@PathVariable String id, @RequestBody TextNode name) {
        apiReportService.rename(id, name.asText(), SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口报告-用例报告删除")
    @CheckOwner(resourceId = "#id", resourceType = "api_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_DELETE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteLog(#id)", msClass = ApiReportLogService.class)
    public void delete(@PathVariable String id) {
        apiReportService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "接口测试-接口报告-用例报告批量删除")
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_DELETE)
    public void batchDelete(@Validated @RequestBody ApiReportBatchRequest request) {
        apiReportService.batchDelete(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "接口测试-接口报告-报告获取")
    @CheckOwner(resourceId = "#id", resourceType = "api_report")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE}, logical = Logical.OR)
    public ApiReportDTO get(@PathVariable String id) {
        return apiReportService.get(id);
    }

    @GetMapping("/share/{shareId}/{reportId}")
    @Operation(summary = "接口测试-接口报告-分享报告获取")
    public ApiReportDTO get(@PathVariable String shareId, @PathVariable String reportId) {
        ShareInfo shareInfo = apiReportShareService.checkResource(shareId);
        apiReportShareService.validateExpired(shareInfo);
        return apiReportService.get(reportId);
    }

    @GetMapping("/get/detail/{reportId}/{stepId}")
    @Operation(summary = "接口测试-接口报告-报告详情获取")
    @CheckOwner(resourceId = "#reportId", resourceType = "api_report")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE}, logical = Logical.OR)
    public List<ApiReportDetailDTO> getDetail(@PathVariable String reportId,
                                              @PathVariable String stepId) {
        return apiReportService.getDetail(reportId, stepId);
    }

    @GetMapping("/share/detail/{shareId}/{reportId}/{stepId}")
    public List<ApiReportDetailDTO> selectReportContent(@PathVariable String shareId,
                                                        @PathVariable String reportId,
                                                        @PathVariable String stepId) {
        ShareInfo shareInfo = apiReportShareService.checkResource(shareId);
        apiReportShareService.validateExpired(shareInfo);
        return apiReportService.getDetail(reportId, stepId);
    }

}
