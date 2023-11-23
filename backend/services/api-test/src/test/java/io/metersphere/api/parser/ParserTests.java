package io.metersphere.api.parser;

import io.metersphere.api.enums.ApiImportPlatform;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ParserTests {

    @Test
    @Order(1)
    public void testImportParserSwagger() throws Exception {
        Objects.requireNonNull(ImportParserFactory.getImportParser(ApiImportPlatform.Swagger3.name())).parse(null, null);
    }

    @Test
    @Order(2)
    public void testImportParserPostman() throws Exception {
        Objects.requireNonNull(ImportParserFactory.getImportParser(ApiImportPlatform.Postman.name())).parse(null, null);
    }

    @Test
    @Order(3)
    public void testImportParserMs() throws Exception {
        ImportParserFactory.getImportParser(ApiImportPlatform.MeterSphere.name());
    }
}
