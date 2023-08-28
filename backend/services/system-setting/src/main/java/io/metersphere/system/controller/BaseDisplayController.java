package io.metersphere.system.controller;

import io.metersphere.system.service.BaseDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "首页图片")
@RestController
@RequestMapping(value = "/base-display")
public class BaseDisplayController {

    @Resource
    private BaseDisplayService baseDisplayService;


    @Operation(summary = "首页-获取icon图片")
    @GetMapping("/get/icon")
    public ResponseEntity<byte[]> getIcon() throws IOException {
        return baseDisplayService.getFile("icon");
    }


    @Operation(summary = "首页-获取loginImage图片")
    @GetMapping("/get/login-image")
    public ResponseEntity<byte[]> getLoginImage() throws IOException {
        return baseDisplayService.getFile("loginImage");
    }

    @Operation(summary = "首页-获取loginLogo图片")
    @GetMapping("/get/login-logo")
    public ResponseEntity<byte[]> getLoginLogo() throws IOException {
        return baseDisplayService.getFile("loginLogo");
    }

    @Operation(summary = "首页-获取logoPlatform图片")
    @GetMapping("/get/logo-platform")
    public ResponseEntity<byte[]> getLogoPlatform() throws IOException {
        return baseDisplayService.getFile("logoPlatform");
    }
}
