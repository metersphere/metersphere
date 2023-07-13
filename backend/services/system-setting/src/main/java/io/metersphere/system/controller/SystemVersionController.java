package io.metersphere.system.controller;

import io.metersphere.system.service.SystemVersionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/system/version")
@RestController
public class SystemVersionController {

    @Resource
    private SystemVersionService systemVersionService;

    @GetMapping("/current")
    public String getVersion() {
        return systemVersionService.getVersion();
    }
}
