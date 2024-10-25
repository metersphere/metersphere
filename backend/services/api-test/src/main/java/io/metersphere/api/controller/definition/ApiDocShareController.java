package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiDocShareDTO;
import io.metersphere.api.dto.definition.ApiDocShareDetail;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.api.service.definition.ApiDefinitionExportService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiDocShareLogService;
import io.metersphere.api.service.definition.ApiDocShareService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping(value = "/api/doc/share")
@Tag(name = "接口测试-定义-分享")
public class ApiDocShareController {

	@Resource
	private ApiDocShareService apiDocShareService;
	@Resource
	private ApiDefinitionExportService apiDefinitionExportService;
	@Resource
	private ApiTestService apiTestService;
	@Resource
	private ApiDefinitionService apiDefinitionService;

	@PostMapping(value = "/page")
	@Operation(summary = "接口测试-定义-分页获取分享列表")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_SHARE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<ApiDocShareDTO>> page(@Validated @RequestBody ApiDocSharePageRequest request) {
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
		return PageUtils.setPageInfo(page, apiDocShareService.list(request));
	}

	@PostMapping(value = "/add")
	@Operation(summary = "接口测试-定义-新增分享")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_SHARE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	@Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDocShareLogService.class)
	public ApiDocShare add(@Validated({Created.class}) @RequestBody ApiDocShareEditRequest request) {
		return apiDocShareService.create(request, SessionUtils.getUserId());
	}

	@PostMapping(value = "/update")
	@Operation(summary = "接口测试-定义-更新分享")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_SHARE)
	@CheckOwner(resourceId = "#request.getId()", resourceType = "api_doc_share")
	@Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDocShareLogService.class)
	public ApiDocShare update(@Validated({Updated.class}) @RequestBody ApiDocShareEditRequest request) {
		return apiDocShareService.update(request, SessionUtils.getUserId());
	}

	@GetMapping("/delete/{id}")
	@Operation(summary = "接口测试-定义-删除分享")
	@Parameter(name = "id", description = "分享ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_SHARE)
	@CheckOwner(resourceId = "#id", resourceType = "api_doc_share")
	@Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiDocShareLogService.class)
	public void delete(@PathVariable String id) {
		apiDocShareService.delete(id);
	}

	@PostMapping("/check")
	@Operation(summary = "接口测试-定义-校验分享密码")
	public Boolean delete(@Validated @RequestBody ApiDocShareCheckRequest request) {
		return apiDocShareService.check(request);
	}

	@GetMapping("/detail/{id}")
	@Operation(summary = "接口测试-定义-分享-查看链接")
	@Parameter(name = "id", description = "分享ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public ApiDocShareDetail detail(@PathVariable String id) {
		return apiDocShareService.detail(id);
	}

	@PostMapping("/module/tree")
	@Operation(summary = "接口测试-定义-分享-模块树")
	public List<BaseTreeNode> getShareDocTree(@Validated @RequestBody ApiDocShareModuleRequest request) {
		List<ProtocolDTO> protocols = apiTestService.getProtocols(request.getOrgId());
		List<String> protocolList = protocols.stream().map(ProtocolDTO::getProtocol).toList();
		request.setProtocols(protocolList);
		return apiDocShareService.getShareTree(request);
	}

	@PostMapping("/module/count")
	@Operation(summary = "接口测试-定义-分享-模块树数量")
	public Map<String, Long> getShareDocTreeCount(@Validated @RequestBody ApiDocShareModuleRequest request) {
		List<ProtocolDTO> protocols = apiTestService.getProtocols(request.getOrgId());
		List<String> protocolList = protocols.stream().map(ProtocolDTO::getProtocol).toList();
		request.setProtocols(protocolList);
		return apiDocShareService.getShareTreeCount(request);
	}

	@PostMapping("/export/{type}")
	@Operation(summary = "接口测试-定义-分享-导出")
	@Parameter(name = "type", description = "导出类型", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public String export(@RequestBody ApiDocShareExportRequest request, @PathVariable String type) {
		List<ProtocolDTO> protocols = apiTestService.getProtocols(request.getOrgId());
		List<String> protocolList = protocols.stream().map(ProtocolDTO::getProtocol).toList();
		request.setProtocols(protocolList);
		return apiDocShareService.export(request, type, SessionUtils.getUserId());
	}

	@GetMapping("/stop/{taskId}")
	@Operation(summary = "接口测试-定义-分享-导出-停止导出")
	@Parameter(name = "taskId", description = "导出任务ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public void caseStopExport(@PathVariable String taskId) {
		apiDefinitionExportService.stopExport(taskId, SessionUtils.getUserId());
	}

	@GetMapping(value = "/download/file/{projectId}/{fileId}")
	@Operation(summary = "接口测试-定义-分享-导出-下载文件")
	@Parameters({
			@Parameter(name = "projectId", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
			@Parameter(name = "fileId", description = "文件ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	})
	public void downloadImgById(@PathVariable String projectId, @PathVariable String fileId, HttpServletResponse httpServletResponse) {
		apiDefinitionExportService.downloadFile(projectId, fileId, SessionUtils.getUserId(), httpServletResponse);
	}

	@GetMapping(value = "/get-detail/{id}")
	@Operation(summary = "接口测试-接口管理-获取接口详情")
	@Parameter(name = "id", description = "接口定义ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public ApiDefinitionDTO get(@PathVariable String id) {
		return apiDefinitionService.get(id, "admin");
	}

	@GetMapping("/plugin/script/{id}/{orgId}")
	@Operation(summary = "获取定义的插件脚本")
	@Parameters({
			@Parameter(name = "id", description = "接口定义ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
			@Parameter(name = "orgId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	})
	public Object getApiProtocolScript(@PathVariable String id, @PathVariable String orgId) {
		return apiDocShareService.getApiProtocolScript(id, orgId);
	}

}
