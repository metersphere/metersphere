package io.metersphere.project.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.FakeError;
import io.metersphere.project.dto.FakeErrorDTO;
import io.metersphere.project.dto.filemanagement.request.FakeErrorDelRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorStatusRequest;
import io.metersphere.project.service.FakeErrorService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "项目管理-项目与权限-接口测试-误报规则配置")
@RestController
@RequestMapping(value = "/fake/error")
public class FakeErrorController {

    @Resource
    private FakeErrorService fakeErrorService;


    @PostMapping("/add")
    @Operation(summary = "项目与权限-接口测试-新增误报规则")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    public ResultHolder add(@Validated @RequestBody List<FakeErrorDTO> dto) {
        String userId = SessionUtils.getUserId();
        return fakeErrorService.save(dto, userId);
    }


    @PostMapping("/list")
    @Operation(summary = "项目与权限-接口测试-获取误报规则列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public Pager<List<FakeError>> list(@Validated @RequestBody FakeErrorRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, fakeErrorService.list(request));
    }


    @PostMapping("/update")
    @Operation(summary = "项目与权限-接口测试-编辑误报规则")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    public ResultHolder update(@Validated @RequestBody List<FakeErrorDTO> requests) {
        String userId = SessionUtils.getUserId();
        return fakeErrorService.update(requests, userId);
    }


    @PostMapping("/delete")
    @Operation(summary = "应用设置-接口测试-删除误报规则")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteLog(#request)", msClass = FakeErrorService.class)
    public void delete(@Validated @RequestBody FakeErrorDelRequest request) {
        fakeErrorService.delete(request);
    }


    @PostMapping("/update/enable")
    @Operation(summary = "项目与权限-接口测试-启用/禁用误报规则")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateEnableLog(#request)", msClass = FakeErrorService.class)
    public void updateEnable(@Validated @RequestBody FakeErrorStatusRequest request) {
        String userId = SessionUtils.getUserId();
        fakeErrorService.updateEnable(request, userId);
    }


}
