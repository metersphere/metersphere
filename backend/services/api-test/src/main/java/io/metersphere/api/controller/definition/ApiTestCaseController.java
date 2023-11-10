package io.metersphere.api.controller.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.service.definition.ApiTestCaseLogService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
    public ApiTestCase add(@Validated @RequestPart("request") ApiTestCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return apiTestCaseService.addCase(request, files, SessionUtils.getUserId());
    }


}
