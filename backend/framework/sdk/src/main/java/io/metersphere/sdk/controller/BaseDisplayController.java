package io.metersphere.sdk.controller;

import io.metersphere.sdk.service.BaseDisplayService;
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
@RequestMapping(value = "/display")
public class BaseDisplayController {

    @Resource
    private BaseDisplayService displayService;


    @Operation(summary = "获取icon图片")
    @GetMapping("/get/icon")
    public ResponseEntity<byte[]> getIcon() throws IOException {
        return displayService.getFile("icon");
    }


    @Operation(summary = "获取loginImage图片")
    @GetMapping("/get/loginImage")
    public ResponseEntity<byte[]> getLoginImage() throws IOException {
        return displayService.getFile("loginImage");
    }

    @Operation(summary = "获取loginLogo图片")
    @GetMapping("/get/loginLogo")
    public ResponseEntity<byte[]> getLoginLogo() throws IOException {
        return displayService.getFile("loginLogo");
    }

    @Operation(summary = "获取logoPlatform图片")
    @GetMapping("/get/logoPlatform")
    public ResponseEntity<byte[]> getLogoPlatform() throws IOException {
        return displayService.getFile("logoPlatform");
    }
}
