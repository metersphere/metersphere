package io.metersphere.api.controller;

import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.MockConfigService;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/4/12 5:11 下午
 * @Description
 */
@RestController
@RequestMapping("/mock")
public class MockApiController {

    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping("/{apiId}/**")
    public String postRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = mockConfigService.getPostParamMap(request);
        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @GetMapping("/{apiId}/**")
    public String getRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = mockConfigService.getGetParamMap(request, apiId);
        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @PutMapping("/{apiId}/**")
    public String putRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = mockConfigService.getPostParamMap(request);

        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @PatchMapping("/{apiId}/**")
    public String patchRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = mockConfigService.getPostParamMap(request);
        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @DeleteMapping("/{apiId}/**")
    public String deleteRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> paramMap = mockConfigService.getGetParamMap(request, apiId);

        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @RequestMapping(value = "/{apiId}/**", method = RequestMethod.OPTIONS)
    public String optionsRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> paramMapPost = mockConfigService.getPostParamMap(request);
        Map<String, String> paramMapGet = mockConfigService.getGetParamMap(request, apiId);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.putAll(paramMapPost);
        paramMap.putAll(paramMapGet);

        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }

        return returnStr;
    }

    @RequestMapping(value = "/{apiId}/**", method = RequestMethod.HEAD)
    public void headRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = mockConfigService.getGetParamMap(request, apiId);
        String returnStr = "";
        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
            response.setStatus(404);
            if (finalExpectConfig != null) {
                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
            }
        }
    }

//    @ConnectMapping("/{apiId}/**")
//    public String conntRequest(@PathVariable String apiId, HttpServletRequest request, HttpServletResponse response) {
//        Enumeration<String> paramNameItor = request.getParameterNames();
//
//        Map<String, String> paramMap = new HashMap<>();
//        while (paramNameItor.hasMoreElements()) {
//            String key = paramNameItor.nextElement();
//            String value = request.getParameter(key);
//            paramMap.put(key, value);
//        }
//
//        String returnStr = "";
//        MockConfigResponse mockConfigData = mockConfigService.findByApiId(apiId);
//        if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
//            MockExpectConfigResponse finalExpectConfig = mockConfigService.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
//            response.setStatus(404);
//            if (finalExpectConfig != null) {
//                returnStr = mockConfigService.updateHttpServletResponse(finalExpectConfig, response);
//            }
//        }
//
//        return returnStr;
//    }

    private static final Map<String, RSocketRequester> REQUESTER_MAP = new HashMap<>();

    @ConnectMapping("/{apiId}/**")
    void onConnect(RSocketRequester rSocketRequester, @Payload String apiId) {
        System.out.println("ooooooo");
        rSocketRequester.rsocket()
                .onClose()
                .subscribe(null, null,
                        () -> REQUESTER_MAP.remove(apiId, rSocketRequester));
        REQUESTER_MAP.put(apiId, rSocketRequester);
    }
}
