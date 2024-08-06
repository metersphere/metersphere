package io.metersphere.api.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.ProtocolDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

public class ApiTestControllerTest {
    @Mock
    ApiTestService apiTestService;
    @Mock
    ApiExecuteService apiExecuteService;
    @InjectMocks
    ApiTestController apiTestController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProtocols() {
        when(apiTestService.getProtocols(anyString())).thenReturn(List.of(new ProtocolDTO()));

        List<ProtocolDTO> result = apiTestController.getProtocols("organizationId");
        Assert.assertEquals(List.of(new ProtocolDTO()), result);
    }

    @Test
    public void testMock() {
        String result = apiTestController.mock(new TextNode("v"));
        Assert.assertEquals("v", result);
    }

    @Test
    public void testRun() {
        when(apiExecuteService.runScript(any(CustomFunctionRunRequest.class))).thenReturn(new TaskRequestDTO());

        TaskRequestDTO result = apiTestController.run(new CustomFunctionRunRequest());
        Assert.assertEquals(new TaskRequestDTO(), result);
    }

    @Test
    public void testGetApiProtocolScript() {
        when(apiTestService.getApiProtocolScript(anyString())).thenReturn("getApiProtocolScriptResponse");

        Object result = apiTestController.getApiProtocolScript("pluginId");
        Assert.assertEquals("getApiProtocolScriptResponse", result);
    }

    @Test
    public void testGetFormOptions() {
        when(apiTestService.getFormOptions(any(ApiTestPluginOptionRequest.class))).thenReturn(List.of(new ApiPluginSelectOption()));

        List<ApiPluginSelectOption> result = apiTestController.getFormOptions(new ApiTestPluginOptionRequest());
        Assert.assertEquals(List.of(new ApiPluginSelectOption()), result);
    }

    @Test
    public void testGetEnvList() {
        when(apiTestService.getEnvList(anyString())).thenReturn(List.of(new Environment()));

        List<Environment> result = apiTestController.getEnvList("projectId");
        Assert.assertEquals(List.of(new Environment()), result);
    }

    @Test
    public void testGetEnvironmentConfig() {
        when(apiTestService.getEnvironmentConfig(anyString())).thenReturn(new EnvironmentConfig());

        EnvironmentConfig result = apiTestController.getEnvironmentConfig("environmentId");
        Assert.assertEquals(new EnvironmentConfig(), result);
    }

    @Test
    public void testGetPool() {
        when(apiTestService.getPoolOption(anyString())).thenReturn(List.of(new TestResourcePool()));

        List<TestResourcePool> result = apiTestController.getPool("projectId");
        Assert.assertEquals(List.of(new TestResourcePool()), result);
    }

    @Test
    public void testGetPoolId() {
        when(apiTestService.getPoolId(anyString())).thenReturn("getPoolIdResponse");

        String result = apiTestController.getPoolId("projectId");
        Assert.assertEquals("getPoolIdResponse", result);
    }
}

//Generated with love by TestMe :) Please raise issues & feature requests at: https://weirddev.com/forum#!/testme