package io.metersphere.functional.controller;

import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalCaseTrashService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用例管理-功能用例-回收站")
@RestController
@RequestMapping("/functional/case/trash")
public class FunctionalCaseTrashController {
    @Resource
    private FunctionalCaseTrashService functionalCaseTrashService;

    @GetMapping("/recover/{id}")
    @Operation(summary = "用例管理-功能用例-回收站-恢复用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = FunctionalCaseLogService.class)
    public void recoverCase(@PathVariable String id) {
        functionalCaseTrashService.recoverCase(id, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "用例管理-功能用例-回收站-彻底删除用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteTrashCaseLog(#id)", msClass = FunctionalCaseLogService.class)
    public void deleteCase(@PathVariable String id) {
        functionalCaseTrashService.deleteCase(id);
    }

}
