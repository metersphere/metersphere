package io.metersphere.api.parser;

import io.metersphere.api.constants.ApiImportPlatform;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ParserTests {

    @Test
    @Order(3)
    public void testImportParserMs() throws Exception {
        ImportParserFactory.getApiDefinitionImportParser(ApiImportPlatform.MeterSphere.name());
    }
}
