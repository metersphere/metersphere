package io.metersphere.api.utils;

import io.metersphere.api.parser.ms.MsTestElementParser;
import io.metersphere.plugin.api.spi.MsTestElement;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  11:46
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class MsTestElementParserTest {

    @Test
    public void testParse() throws Exception {
        File httpJmx = new File(
                this.getClass().getClassLoader().getResource("file/http.jmx")
                        .getPath()
        );
        Object scriptWrapper = SaveService.loadElement(new FileInputStream(httpJmx));
        HashTree hashTree = getHashTree(scriptWrapper);
        MsTestElementParser parser = new MsTestElementParser();
        MsTestElement msTestElement = parser.parse(hashTree);
        Assertions.assertNotNull(msTestElement);
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        ReflectionUtils.makeAccessible(field);
        return (HashTree) field.get(scriptWrapper);
    }
}
