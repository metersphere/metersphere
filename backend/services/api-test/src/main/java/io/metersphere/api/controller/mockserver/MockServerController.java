package io.metersphere.api.controller.mockserver;

import io.metersphere.api.service.mockserver.MockServerService;
import io.metersphere.api.utils.MockServerUtils;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.system.controller.handler.annotation.NoResultHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/mock-server")
@Tag(name = "接口测试-接口管理-接口定义-Mock")
public class MockServerController {

    @Resource
    private MockServerService mockServerService;

    @RequestMapping(value = "/{projectNum}/{apiInfo}/**", method = RequestMethod.OPTIONS)
    @NoResultHolder
    public Object optionsRequest(@PathVariable String projectNum, @PathVariable String apiInfo, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        return mockServerService.execute(HttpMethodConstants.OPTIONS.name(), requestHeaderMap, projectNum, apiInfo, request, response);
    }

    @RequestMapping(value = "/{projectNum}/{apiInfo}/**")
    @NoResultHolder
    public Object mockRequest(@PathVariable String projectNum, @PathVariable String apiInfo, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        String method = request.getMethod();
        return mockServerService.execute(method, requestHeaderMap, projectNum, apiInfo, request, response);
    }
}
