package io.metersphere.controller;


import io.metersphere.service.EnvironmentGroupProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/environment/group/project")
public class EnvironmentGroupProject {

    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    @GetMapping("/json/{groupId}")
    public String getEnvJSON(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvJson(groupId);
    }

    @GetMapping("/map/{groupId}")
    public Map<String, String> getEnvMap(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvMap(groupId);
    }

    @GetMapping("/map/name/{groupId}")
    public Map<String, String> getEnvNameMap(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvNameMap(groupId);
    }
}
