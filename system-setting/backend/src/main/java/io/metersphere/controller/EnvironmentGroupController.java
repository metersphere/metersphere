package io.metersphere.controller;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.environment.dto.EnvironmentGroupRequest;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.EnvironmentGroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(value = "/environment/group")
public class EnvironmentGroupController {

    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    @PostMapping("/add")
    public EnvironmentGroup add(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.add(request);
    }

    @PostMapping("/batch/add")
    public void batchAdd(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.batchAdd(request);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        environmentGroupService.delete(id);
    }

    @PostMapping("/update")
    public EnvironmentGroup update(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.update(request);
    }

    @PostMapping("/modify")
    public void modify(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.modify(request);
    }

    @GetMapping("/copy/{id}")
    public void copy(@PathVariable String id) {
        environmentGroupService.copy(id);
    }

    @GetMapping("/project/map/name/{groupId}")
    public Map<String, String> getEnvNameMap(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvNameMap(groupId);
    }
}
