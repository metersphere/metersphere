package io.metersphere.system.controller;

import io.metersphere.system.service.SystemVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统版本")
@RequestMapping("/system/version")
@RestController
public class SystemVersionController {

    @Resource
    private SystemVersionService systemVersionService;

    @GetMapping("/current")
    @Operation(summary = "获取当前系统版本")
    public String getVersion() {
        return systemVersionService.getVersion();
    }

    @GetMapping("/package-type")
    @Operation(summary = "获取当前系统类型")
    public String getPackageType() {
        return systemVersionService.getPackageType();
    }
}
