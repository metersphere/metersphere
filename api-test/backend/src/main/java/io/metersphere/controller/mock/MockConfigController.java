package io.metersphere.controller.mock;

import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.mock.ApiDefinitionResponseDTO;
import io.metersphere.api.dto.mock.MockParamSuggestions;
import io.metersphere.api.dto.mock.MockTestDataRequest;
import io.metersphere.api.dto.mock.config.MockConfigRequest;
import io.metersphere.api.dto.mock.config.MockExpectConfigRequest;
import io.metersphere.api.dto.mock.config.response.MockConfigResponse;
import io.metersphere.api.dto.mock.config.response.MockExpectConfigResponse;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.MockExpectConfig;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.mock.MockApiUtils;
import io.metersphere.commons.utils.mock.MockTestDataUtil;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.MockConfigService;
import io.metersphere.service.definition.ApiDefinitionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/4/12 5:11 下午
 * @Description
 */
@RestController
@RequestMapping("/mock-config")
public class MockConfigController {

    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping("/gen")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_MOCK)
    public MockConfigResponse genMockConfig(@RequestBody MockConfigRequest request) {
        return mockConfigService.genMockConfig(request);
    }

    @PostMapping(value = "/update/form", consumes = {"multipart/form-data"})
    @MsRequestLog(module = OperLogModule.API_DEFINITION)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_MOCK)
    public MockExpectConfig updateMockExpectConfig(@RequestPart("request") MockExpectConfigRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return mockConfigService.updateMockExpectConfig(request, bodyFiles);
    }

    @PostMapping(value = "/update/expect")
    @MsRequestLog(module = OperLogModule.API_DEFINITION)
    public MockExpectConfig updateMockExpectConfig(@RequestBody MockExpectConfigRequest request) {
        return mockConfigService.updateMockExpectConfigStatus(request);
    }

    @GetMapping("/get-expect/{id}")
    public MockExpectConfigResponse selectMockExpectConfig(@PathVariable String id) {
        MockExpectConfigWithBLOBs config = mockConfigService.findMockExpectConfigById(id);
        MockExpectConfigResponse response = new MockExpectConfigResponse(config);
        return response;
    }

    @GetMapping("/delete/{id}")
    @MsRequestLog(module = OperLogModule.API_DEFINITION)
    public String deleteMockExpectConfig(@PathVariable String id) {
        mockConfigService.deleteMockExpectConfig(id);
        return "SUCCESS";
    }

    @GetMapping("/get-api-params/{id}")
    public Map<String, List<MockParamSuggestions>> getApiParams(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionService.getBLOBs(id);
        Map<String, List<MockParamSuggestions>> apiParams = mockConfigService.getApiParamsByApiDefinitionBLOBs(apiDefinitionWithBLOBs);
        return apiParams;
    }

    @GetMapping("/get-api-response/{id}")
    public ApiDefinitionResponseDTO getApiResponse(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionService.getBLOBs(id);
        ApiDefinitionResponseDTO returnMap = MockApiUtils.getApiResponse(apiDefinitionWithBLOBs.getResponse());
        return returnMap;
    }

    @PostMapping("/test-data")
    public List<MockTestDataRequest> getMockTestData(@RequestBody List<MockTestDataRequest> requestArray) {
        MockTestDataUtil testDataUtil = new MockTestDataUtil();
        return testDataUtil.parseTestDataByRequest(requestArray);
    }

    @PostMapping("/get-tcp-test-data")
    public List<TcpTreeTableDataStruct> getTcpMockTestData(@RequestBody List<TcpTreeTableDataStruct> requestArray) {
        MockTestDataUtil testDataUtil = new MockTestDataUtil();
        return testDataUtil.parseTestDataByTcpTreeTableData(requestArray);
    }

    @GetMapping("/close/tcp/{port}")
    public void closeTcpPort(@PathVariable int port) {
        mockConfigService.closeTcpPort(port);
    }

    @GetMapping("/open/tcp/{port}")
    public void openTcpPort(@PathVariable int port) {
        mockConfigService.openTcpPort(port);
    }

    @GetMapping("/get-details/{projectId}")
    public String getMockInfo(@PathVariable(value = "projectId") String projectId) {
        return mockConfigService.getMockInfo(projectId);
    }
}
