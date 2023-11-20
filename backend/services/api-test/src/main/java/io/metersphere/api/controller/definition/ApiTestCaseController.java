package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.service.definition.ApiTestCaseLogService;
import io.metersphere.api.service.definition.ApiTestCaseNoticeService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(value = "/api/testCase")
@Tag(name = "接口测试-接口管理-接口用例")
public class ApiTestCaseController {
    @Resource
    private ApiTestCaseService apiTestCaseService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-接口用例-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiTestCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_CREATE, target = "#targetClass.getCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
    public ApiTestCase add(@Validated @RequestPart("request") ApiTestCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return apiTestCaseService.addCase(request, files, SessionUtils.getUserId());
    }

    @GetMapping(value = "/get-detail/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-获取详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public ApiTestCaseDTO get(@PathVariable String id) {
        return apiTestCaseService.get(id, SessionUtils.getUserId());
    }

    @GetMapping("/move-gc/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-移动到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.moveToGcLog(#id)", msClass = ApiTestCaseLogService.class)
    public void deleteToGc(@PathVariable String id) {
        apiTestCaseService.deleteToGc(id, SessionUtils.getUserId());
    }

    @GetMapping("recover/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_RECOVER)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = ApiTestCaseLogService.class)
    public void recover(@PathVariable String id) {
        apiTestCaseService.recover(id);
    }

    @GetMapping("follow/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.followLog(#id)", msClass = ApiTestCaseLogService.class)
    public void follow(@PathVariable String id) {
        apiTestCaseService.follow(id, SessionUtils.getUserId());
    }

    @GetMapping("unfollow/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-取消关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.unfollowLog(#id)", msClass = ApiTestCaseLogService.class)
    public void unfollow(@PathVariable String id) {
        apiTestCaseService.unfollow(id, SessionUtils.getUserId());
    }

    @GetMapping("delete/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiTestCaseLogService.class)
    public void delete(@PathVariable String id) {
        apiTestCaseService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-接口用例-更新")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiTestCaseLogService.class)
    public ApiTestCase update(@Validated @RequestPart("request") ApiTestCaseUpdateRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return apiTestCaseService.update(request, files, SessionUtils.getUserId());
    }

    @GetMapping(value = "/update-status/{id}/{status}")
    @Operation(summary = "接口测试-接口管理-接口用例-更新状态")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiTestCaseLogService.class)
    public void updateStatus(@PathVariable String id, @PathVariable String status) {
        apiTestCaseService.updateStatus(id, status, SessionUtils.getUserId());
    }

    @PostMapping(value = "/page")
    @Operation(summary = "接口测试-接口管理-接口用例-分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public Pager<List<ApiTestCaseDTO>> page(@Validated @RequestBody ApiTestCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.page(request));
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    public void deleteBatchByParam(@RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/move-gc")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    public void deleteToGcByParam(@RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseService.batchMoveGc(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    public void batchUpdate(@Validated @RequestBody ApiCaseBatchEditRequest request) {
        apiTestCaseService.batchEdit(request, SessionUtils.getUserId());
    }


}
