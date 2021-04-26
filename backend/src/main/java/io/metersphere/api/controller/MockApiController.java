package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @PostMapping("/{projectId}/**")
    @NoResultHolder
    public String postRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("POST", projectId, request, response);
        return returnStr;
    }

    @GetMapping("/{projectId}/**")
    @NoResultHolder
    public String getRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("GET", projectId, request, response);
        return returnStr;
    }

    @PutMapping("/{projectId}/**")
    @NoResultHolder
    public String putRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PUT", projectId, request, response);
        return returnStr;
    }

    @PatchMapping("/{projectId}/**")
    @NoResultHolder
    public String patchRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PATCH", projectId, request, response);
        return returnStr;
    }

    @DeleteMapping("/{projectId}/**")
    @NoResultHolder
    public String deleteRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("DELETE", projectId, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectId}/**", method = RequestMethod.OPTIONS)
    @NoResultHolder
    public String optionsRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("OPTIONS", projectId, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectId}/**", method = RequestMethod.HEAD)
    @NoResultHolder
    public void headRequest(@PathVariable String projectId, HttpServletRequest request, HttpServletResponse response) {
        mockConfigService.checkReturnWithMockExpectByUrlParam("HEAD", projectId, request, response);
    }
}
