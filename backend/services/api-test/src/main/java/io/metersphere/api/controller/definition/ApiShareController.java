package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.definition.ApiDefinitionDocDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDocRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.service.ApiShareService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author: LAN
 * @date: 2023/12/27 14:30
 * @version: 1.0
 */
@RestController
@RequestMapping(value = "/api/share")
@Tag(name = "接口测试-接口管理-接口分享")
public class ApiShareController {

    @Resource
    private ApiShareService apiShareService;

    @PostMapping("/doc/gen")
    @Operation(summary = "接口测试-接口管理-接口文档分享")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ShareInfoDTO genApiDocShareInfo(@RequestBody ApiDefinitionDocRequest request) {
        return apiShareService.genApiDocShareInfo(request, Objects.requireNonNull(SessionUtils.getUser()));
    }

    @GetMapping("/doc/view/{shareId}")
    @Operation(summary = "接口测试-接口管理-接口文档分享查看")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#shareId", resourceType = "share_info")
    public ApiDefinitionDocDTO shareDocView(@PathVariable String shareId) {
        return apiShareService.shareDocView(shareId);
    }
}
