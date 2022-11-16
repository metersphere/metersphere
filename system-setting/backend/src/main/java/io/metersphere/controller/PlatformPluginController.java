package io.metersphere.controller;

import io.metersphere.domain.SelectOption;
import io.metersphere.dto.PlatformProjectOptionRequest;
import io.metersphere.service.PlatformPluginService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/platform/plugin")
public class PlatformPluginController {

    @Resource
    private PlatformPluginService platformPluginService;

    @GetMapping("/integration/info")
    public Object getIntegrationInfo() {
        return platformPluginService.getIntegrationInfo();
    }

    @GetMapping("/project/info/{key}")
    public Object getProjectInfo(@PathVariable("key") String key) {
        return platformPluginService.getProjectInfo(key);
    }

    @GetMapping("/resource/{pluginId}")
    public void getPluginResource(@PathVariable("pluginId") String pluginId, @RequestParam("fileName") String fileName, HttpServletResponse response) {
        platformPluginService.getPluginResource(pluginId, fileName, response);
    }

    @PostMapping("/integration/validate/{pluginId}")
    public void validateIntegration(@PathVariable("pluginId") String pluginId, @RequestBody Map config) {
        platformPluginService.validateIntegration(pluginId, config);
    }
    @PostMapping("/project/validate/{pluginId}")
    public void validateProjectConfig(@PathVariable("pluginId") String pluginId, @RequestBody Map config) {
        platformPluginService.validateProjectConfig(pluginId, config);
    }

    @PostMapping("/project/option")
    public List<SelectOption> getProjectOption(@RequestBody PlatformProjectOptionRequest request) {
       return platformPluginService.getProjectOption(request);
    }

    @GetMapping("/platform/option")
    public List<SelectOption> getPlatformOptions() {
        return platformPluginService.getPlatformOptions();
    }

    @GetMapping("/template/support/list")
    public List<String> getThirdPartTemplateSupportPlatform() {
        return platformPluginService.getThirdPartTemplateSupportPlatform();
    }
}
