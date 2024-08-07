package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.dto.sdk.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.system.dto.user.UserExcludeOptionDTO;
import io.metersphere.system.dto.user.UserRoleRelationUserDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.GlobalUserRoleRelationLogService;
import io.metersphere.system.service.GlobalUserRoleRelationService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-6-12
 */
@RestController
@Tag(name = "系统设置-系统-用户组-用户关联关系")
@RequestMapping("/user/role/relation/global")
public class GlobalUserRoleRelationController {

    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;

    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取全局用户组对应的用户列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public Pager<List<UserRoleRelationUserDTO>> list(@Validated @RequestBody GlobalUserRoleRelationQueryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, globalUserRoleRelationService.list(request));
    }

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-创建全局用户组和用户的关联关系")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = GlobalUserRoleRelationLogService.class)
    public void add(@Validated({Created.class}) @RequestBody GlobalUserRoleRelationUpdateRequest request) {
        request.setCreateUser(SessionUtils.getUserId());
        globalUserRoleRelationService.add(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-删除全局用户组和用户的关联关系")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = GlobalUserRoleRelationLogService.class)
    public void delete(@PathVariable String id) {
        globalUserRoleRelationService.delete(id);
    }

    @GetMapping("/user/option/{roleId}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取需要关联的用户选项")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_USER_ROLE_READ})
    public List<UserExcludeOptionDTO> getSelectOption(@Schema(description = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @PathVariable String roleId,
                                                      @Schema(description = "查询关键字，根据邮箱和用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return globalUserRoleRelationService.getExcludeSelectOption(roleId, keyword);
    }
}
