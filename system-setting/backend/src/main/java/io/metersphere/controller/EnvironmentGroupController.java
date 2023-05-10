package io.metersphere.controller;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.environment.dto.EnvironmentGroupRequest;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.EnvironmentGroupService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(value = "/environment/group")
public class EnvironmentGroupController {

    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    @PostMapping("/add")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    public EnvironmentGroup add(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.add(request);
    }

    @PostMapping("/batch/add")
    public void batchAdd(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.batchAdd(request);
    }

    @GetMapping("/delete/{id}")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    public void delete(@PathVariable String id) {
        environmentGroupService.delete(id);
    }

    @PostMapping("/update")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    public EnvironmentGroup update(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.update(request);
    }

    @PostMapping("/modify")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    public void modify(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.modify(request);
    }

    @GetMapping("/copy/{id}")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    public void copy(@PathVariable String id) {
        environmentGroupService.copy(id);
    }

    @GetMapping("/project/map/name/{groupId}")
    public Map<String, String> getEnvNameMap(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvNameMap(groupId);
    }
}
