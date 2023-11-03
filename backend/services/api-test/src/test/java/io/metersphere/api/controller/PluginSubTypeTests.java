package io.metersphere.api.controller;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.dto.TestElementDTO;
import io.metersphere.sdk.dto.api.request.logic.controller.MsLoopController;
import io.metersphere.sdk.dto.api.request.post.processors.MsPostJSR223Processor;
import io.metersphere.sdk.dto.api.request.sampler.MsDebugSampler;
import io.metersphere.system.uid.IDGenerator;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class PluginSubTypeTests {


    @Test
    @Order(0)
    public void pluginSubTypeTest() throws Exception {
        MsDebugSampler debugSampler = new MsDebugSampler();
        debugSampler.setName("测试DebugSampler");
        debugSampler.setUuid(IDGenerator.nextStr());
        LinkedList<TestElementDTO> hashTree = new LinkedList<>();
        hashTree.add(debugSampler);
        MsPostJSR223Processor msjsr223Processor = new MsPostJSR223Processor();
        msjsr223Processor.setName("测试jsr223");
        msjsr223Processor.setJsrEnable(true);
        msjsr223Processor.setChildren(hashTree);

        String json = ApiDataUtils.toJSONString(msjsr223Processor);
        Assertions.assertNotNull(json);
        TestElementDTO testElementDTO = ApiDataUtils.parseObject(json, TestElementDTO.class);
        Assertions.assertNotNull(testElementDTO);
    }

    @Test
    @Order(1)
    public void resolverTest() throws Exception {
        // ApiDataUtils.setResolver(MsLoopController.class);
        List<NamedType> namedTypes = new LinkedList<>();
        namedTypes.add(new NamedType(MsLoopController.class, MsLoopController.class.getSimpleName()));
        ApiDataUtils.setResolver(namedTypes);
    }

    @Test
    @Order(2)
    public void newPluginSubTypeTest() throws Exception {
        MsLoopController loopController = new MsLoopController();
        loopController.setName("测试loopController");
        String json = ApiDataUtils.toJSONString(loopController);

        TestElementDTO testElementDTO = ApiDataUtils.parseObject(json, TestElementDTO.class);
        Assertions.assertNotNull(testElementDTO);

    }

    @Test
    @Order(3)
    public void retrySubTypeTest() throws Exception {
        MsDebugSampler debugSampler = new MsDebugSampler();
        debugSampler.setName("测试DebugSampler");
        debugSampler.setUuid(IDGenerator.nextStr());
        LinkedList<TestElementDTO> hashTree = new LinkedList<>();
        hashTree.add(debugSampler);
        MsPostJSR223Processor msjsr223Processor = new MsPostJSR223Processor();
        msjsr223Processor.setName("测试jsr223");
        msjsr223Processor.setJsrEnable(true);
        msjsr223Processor.setChildren(hashTree);

        String json = ApiDataUtils.toJSONString(msjsr223Processor);
        Assertions.assertNotNull(json);
        TestElementDTO testElementDTO = ApiDataUtils.parseObject(json, TestElementDTO.class);
        Assertions.assertNotNull(testElementDTO);
    }
}
