package io.metersphere.controller;

import io.metersphere.service.PlatformPluginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;


@RestController
@RequestMapping(value = "/platform/plugin")
public class PlatformPluginController {

    @Resource
    private PlatformPluginService platformPluginService;

    @GetMapping("/integration/info")
    public Object getIntegrationInfo() {
        return platformPluginService.getIntegrationInfo();
    }

    @GetMapping("/resource/{pluginId}")
    public void getPluginResource(@PathVariable("pluginId") String pluginId, @RequestParam("fileName") String fileName, HttpServletResponse response) {
        platformPluginService.getPluginResource(pluginId, fileName, response);
    }
}
