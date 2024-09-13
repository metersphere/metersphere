package io.metersphere.api.service;

import io.metersphere.api.parser.jmeter.xstream.MsSaveService;
import io.metersphere.system.base.BaseApiPluginTestService;
import jakarta.annotation.Resource;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-01  14:04
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class MsSaveServiceTests {

    @Resource
    private BaseApiPluginTestService baseApiPluginTestService;

    @Test
    public void getBlankJon() throws Exception {
        baseApiPluginTestService.addPlugin("file/tcp-sampler-xstream.jar");
        File tcpJmx = new File(
                this.getClass().getClassLoader().getResource("file/tcp.jmx")
                        .getPath()
        );
        Object scriptWrapper = MsSaveService.loadElement(new FileInputStream(tcpJmx));
        getHashTree(scriptWrapper);
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }
}
