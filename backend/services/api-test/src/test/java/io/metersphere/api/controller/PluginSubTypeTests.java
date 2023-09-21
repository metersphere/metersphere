package io.metersphere.api.controller;

import io.metersphere.api.dto.jmeter.processors.MSJSR223Processor;
import io.metersphere.api.dto.jmeter.sampler.MSDebugSampler;
import io.metersphere.api.util.JSONUtils;
import io.metersphere.plugin.api.dto.TestElementDTO;
import io.metersphere.system.uid.UUID;
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
        MSDebugSampler debugSampler = new MSDebugSampler();
        debugSampler.setName("测试DebugSampler");
        debugSampler.setUuid(UUID.randomUUID().toString());
        LinkedList<TestElementDTO> hashTree = new LinkedList<>();
        hashTree.add(debugSampler);
        MSJSR223Processor msjsr223Processor = new MSJSR223Processor();
        msjsr223Processor.setName("测试jsr223");
        msjsr223Processor.setJsrEnable(true);
        msjsr223Processor.setChildren(hashTree);

        String json = JSONUtils.toJSONString(msjsr223Processor);
        Assertions.assertNotNull(json);
        TestElementDTO testElementDTO = JSONUtils.parseObject(json, TestElementDTO.class);
        Assertions.assertNotNull(testElementDTO);
    }
}
