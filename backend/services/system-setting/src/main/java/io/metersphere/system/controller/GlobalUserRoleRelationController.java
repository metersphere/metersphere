package io.metersphere.system.controller;

import java.util.List;

import io.metersphere.system.dto.GlobalUserRoleUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import io.metersphere.validation.groups.*;
import io.metersphere.sdk.util.PageUtils;
import com.github.pagehelper.Page;
import io.metersphere.sdk.util.Pager;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import org.springframework.validation.annotation.Validated;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.service.GlobalUserRoleRelationService;
import io.metersphere.system.domain.UserRoleRelation;

/**
 * @author : jianxing
 * @date : 2023-6-12
 */
@RestController
@Tag(name = "全局用户组与用户的关联关系")
@RequestMapping("/user/role/relation/global")
public class GlobalUserRoleRelationController {

    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;

    @PostMapping("/list")
    @Operation(summary = "获取全局用户组对应的用户列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_RELATION_READ)
    public Pager<List<GlobalUserRoleUserDTO>> list(@RequestBody GlobalUserRoleRelationQueryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, globalUserRoleRelationService.list(request));
    }

    @PostMapping("/add")
    @Operation(summary = "创建全局用户组和用户的关联关系")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_RELATION_ADD)
    public UserRoleRelation add(@Validated({Created.class}) @RequestBody UserRoleRelation userRoleRelation) {
        return globalUserRoleRelationService.add(userRoleRelation);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除全局用户组和用户的关联关系")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_RELATION_DELETE)
    public String delete(@PathVariable String id) {
        return globalUserRoleRelationService.delete(id);
    }
}
