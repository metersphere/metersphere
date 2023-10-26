package io.metersphere.api.controller;

import io.metersphere.api.dto.request.post.processors.MsPostJSR223Processor;
import io.metersphere.api.dto.request.sampler.MsDebugSampler;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.dto.TestElementDTO;
import io.metersphere.system.uid.IDGenerator;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
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
}
