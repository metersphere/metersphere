package io.metersphere.api.controller;

import io.metersphere.api.dto.mock.MockApiUtils;
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
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value ="/updateMockExpectConfig", consumes = {"multipart/form-data"})
    public MockExpectConfig updateMockExpectConfig(@RequestPart("request")MockExpectConfigRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return mockConfigService.updateMockExpectConfig(request,bodyFiles);
    }

    @PostMapping(value ="/updateMockExpectConfigStatus")
    public MockExpectConfig updateMockExpectConfig(@RequestBody MockExpectConfigRequest request) {
        return mockConfigService.updateMockExpectConfigStatus(request);
    }

//    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
//    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_API)
//    @MsAuditLog(module = "api_definition", type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiDefinitionService.class)
//    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CREATE, mailTemplate = "api/DefinitionCreate", subject = "接口定义通知")
//    public ApiDefinitionWithBLOBs create(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
//        checkPermissionService.checkProjectOwner(request.getProjectId());
//        return apiDefinitionService.create(request, bodyFiles);
//    }

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

    @GetMapping("/getApiResponse/{id}")
    public Map<String, String> getApiResponse(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionService.getBLOBs(id);
        Map<String, String> returnMap = MockApiUtils.getApiResponse(apiDefinitionWithBLOBs.getResponse());
        return returnMap;
    }

}
