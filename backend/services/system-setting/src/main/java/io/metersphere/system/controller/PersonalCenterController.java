package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.dto.request.user.PersonalLocaleRequest;
import io.metersphere.system.dto.request.user.PersonalUpdatePasswordRequest;
import io.metersphere.system.dto.request.user.PersonalUpdateRequest;
import io.metersphere.system.dto.user.PersonalDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.SimpleUserService;
import io.metersphere.system.service.SystemAIConfigService;
import io.metersphere.system.service.UserLogService;
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

import java.util.List;

@RestController
@Tag(name = "个人中心")
@RequestMapping("/personal")
public class PersonalCenterController {

    @Resource
    private SimpleUserService simpleUserService;

    @Resource
    private SystemAIConfigService systemAIConfigService;

    @GetMapping("/get/{id}")
    @Operation(summary = "个人中心-获取信息")
    public PersonalDTO getInformation(@PathVariable String id) {
        this.checkPermission(id);
        return simpleUserService.getPersonalById(id);
    }

    @PostMapping("/update-info")
    @Operation(summary = "个人中心-修改信息")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateAccountLog(#request)", msClass = UserLogService.class)
    public boolean updateUser(@Validated @RequestBody PersonalUpdateRequest request) {
        this.checkPermission(request.getId());
        return simpleUserService.updateAccount(request, SessionUtils.getUserId());
    }

    @PostMapping("/update-locale")
    @Operation(summary = "个人中心-修改信息")
    public void updateLocale(@Validated @RequestBody PersonalLocaleRequest request) {
        simpleUserService.updateLanguage(request, SessionUtils.getUserId());
    }

    @PostMapping("/update-password")
    @Operation(summary = "个人中心-修改密码")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePasswordLog(#request)", msClass = UserLogService.class)
    public String updateUser(@Validated @RequestBody PersonalUpdatePasswordRequest request) {
        this.checkPermission(request.getId());
        if (simpleUserService.updatePassword(request)) {
            SessionUtils.kickOutUser(SessionUtils.getUser().getId());
        }
        return "OK";
    }

    private void checkPermission(String id) {
        if (!StringUtils.equals(id, SessionUtils.getUserId())) {
            throw new MSException("personal.no.permission");
        }
    }

    @PostMapping("/model/edit-source")
    @Operation(summary = "系统设置-编辑模型设置")
    public void editModuleConfig(@Validated @RequestBody AiModelSourceDTO aiModelSourceDTO) {
        systemAIConfigService.editModuleConfig(aiModelSourceDTO, SessionUtils.getUserId());
    }

    @PostMapping("/model/source/list")
    @Operation(summary = "系统设置-查看模型集合")
    public Pager<List<AiModelSourceDTO>> getModelSourceList(@Validated @RequestBody AiModelSourceRequest aiModelSourceRequest) {
        this.checkPermission(aiModelSourceRequest.getOwner());
        Page<Object> page = PageHelper.startPage(aiModelSourceRequest.getCurrent(), aiModelSourceRequest.getPageSize());
        return PageUtils.setPageInfo(page, systemAIConfigService.getModelSourceList(aiModelSourceRequest));
    }

    @GetMapping("/model/get/{id}")
    @Operation(summary = "获取模型信息")
    public AiModelSourceDTO getModelInformation(@PathVariable String id) {
        return systemAIConfigService.getModelSourceDTO(id, SessionUtils.getUserId());
    }

    @GetMapping("/model/delete/{id}")
    @Operation(summary = "删除模型")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AI_MODEL_UPDATE)
    public void delModelInformation(@PathVariable String id) {
        systemAIConfigService.delModelInformation(id, SessionUtils.getUserId());
    }

}
