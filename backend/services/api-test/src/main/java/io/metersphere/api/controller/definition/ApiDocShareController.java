package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.dto.definition.ApiDocShareDTO;
import io.metersphere.api.dto.definition.ApiDocShareDetail;
import io.metersphere.api.dto.definition.request.ApiDocShareCheckRequest;
import io.metersphere.api.dto.definition.request.ApiDocShareEditRequest;
import io.metersphere.api.dto.definition.request.ApiDocSharePageRequest;
import io.metersphere.api.service.definition.ApiDocShareLogService;
import io.metersphere.api.service.definition.ApiDocShareService;
import io.metersphere.sdk.constants.PermissionConstants;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping(value = "/api/doc/share")
@Tag(name = "接口测试-接口管理-分享")
public class ApiDocShareController {

	@Resource
	private ApiDocShareService apiDocShareService;

	@PostMapping(value = "/page")
	@Operation(summary = "接口测试-接口管理-分页获取分享列表")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<ApiDocShareDTO>> page(@Validated @RequestBody ApiDocSharePageRequest request) {
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
		return PageUtils.setPageInfo(page, apiDocShareService.list(request));
	}

	@PostMapping(value = "/add")
	@Operation(summary = "接口测试-接口管理-新增分享")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	@Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDocShareLogService.class)
	public ApiDocShare add(@Validated({Created.class}) @RequestBody ApiDocShareEditRequest request) {
		return apiDocShareService.create(request, SessionUtils.getUserId());
	}

	@PostMapping(value = "/update")
	@Operation(summary = "接口测试-接口管理-更新分享")
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE)
	@CheckOwner(resourceId = "#request.getId()", resourceType = "api_doc_share")
	@Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDocShareLogService.class)
	public ApiDocShare update(@Validated({Updated.class}) @RequestBody ApiDocShareEditRequest request) {
		return apiDocShareService.update(request);
	}

	@GetMapping("/delete/{id}")
	@Operation(summary = "接口测试-接口管理-删除分享")
	@Parameter(name = "id", description = "分享ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	@RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE)
	@CheckOwner(resourceId = "#id", resourceType = "api_doc_share")
	@Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiDocShareLogService.class)
	public void delete(@PathVariable String id) {
		apiDocShareService.delete(id);
	}

	@PostMapping("/check")
	@Operation(summary = "接口测试-接口管理-校验分享密码")
	public Boolean delete(@Validated @RequestBody ApiDocShareCheckRequest request) {
		return apiDocShareService.check(request);
	}

	@GetMapping("/detail/{id}")
	@Operation(summary = "接口测试-接口管理-查看链接")
	@Parameter(name = "id", description = "分享ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public ApiDocShareDetail detail(@PathVariable String id) {
		return apiDocShareService.detail(id);
	}
}
