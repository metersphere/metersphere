package io.metersphere.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.ModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("module")
public class ModuleController {
    @Resource
    private ModuleService moduleService;

    @GetMapping("update/{key}/status/{status}")
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#key,#status)", msClass = ModuleService.class)
    public void updateModuleStatus(@PathVariable("key") String key, @PathVariable("status") String status) {
        moduleService.updateModuleStatus(key, status);

    }
}
