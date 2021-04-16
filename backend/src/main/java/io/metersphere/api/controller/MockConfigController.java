package io.metersphere.api.controller;

import io.metersphere.api.dto.mockconfig.MockConfigRequest;
import io.metersphere.api.dto.mockconfig.MockExpectConfigRequest;
import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.MockExpectConfig;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/4/12 5:11 下午
 * @Description
 */
@RestController
@RequestMapping("/mockConfig")
public class MockConfigController {

    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping("/genMockConfig")
    public MockConfigResponse genMockConfig(@RequestBody MockConfigRequest request) {
        return mockConfigService.genMockConfig(request);
    }

    @PostMapping("/updateMockExpectConfig")
    public MockExpectConfig updateMockExpectConfig(@RequestBody MockExpectConfigRequest request) {
        return mockConfigService.updateMockExpectConfig(request);
    }

    @GetMapping("/mockExpectConfig/{id}")
    public MockExpectConfigResponse selectMockExpectConfig(@PathVariable String id) {
        MockExpectConfigWithBLOBs config = mockConfigService.findMockExpectConfigById(id);
        MockExpectConfigResponse response = new MockExpectConfigResponse(config);
        return response;
    }

    @GetMapping("/deleteMockExpectConfig/{id}")
    public String deleteMockExpectConfig(@PathVariable String id) {
        mockConfigService.deleteMockExpectConfig(id);
        return "SUCCESS";
    }

    @GetMapping("/getApiParams/{id}")
    public List<Map<String, String>> getApiParams(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionService.getBLOBs(id);
        List<Map<String, String>> apiParams = mockConfigService.getApiParamsByApiDefinitionBLOBs(apiDefinitionWithBLOBs);
        return apiParams;
    }
}
