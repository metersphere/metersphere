package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTestService;
import io.metersphere.system.dto.ProtocolDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:54
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "接口测试")
public class ApiTestController {

    @Resource
    private ApiTestService apiTestService;

    @GetMapping("/protocol/{organizationId}")
    @Operation(summary = "获取协议插件的的协议列表")
    public List<ProtocolDTO> getProtocols(@PathVariable String organizationId) {
        return apiTestService.getProtocols(organizationId);
    }
}
