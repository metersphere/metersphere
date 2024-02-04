package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.system.base.BaseApiPluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.ProtocolDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-08  10:32
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestServiceTest extends BaseTest {
    @Resource
    private ApiTestService apiTestService;
    @Resource
    private BaseApiPluginTestService baseApiPluginTestService;

    @Test
    public void getProtocols() throws Exception {
        baseApiPluginTestService.addJdbcPlugin();
        // 校验数据是否正确
        List<ProtocolDTO> protocols = apiTestService.getProtocols(this.DEFAULT_ORGANIZATION_ID);
        List expected = new ArrayList<ProtocolDTO>();
        ProtocolDTO httpProtocol = new ProtocolDTO();
        httpProtocol.setProtocol(ApiConstants.HTTP_PROTOCOL);
        httpProtocol.setPolymorphicName(MsHTTPElement.class.getSimpleName());
        ProtocolDTO jdbcProtocol = new ProtocolDTO();
        jdbcProtocol.setProtocol("JDBC");
        jdbcProtocol.setPluginId("jdbc");
        jdbcProtocol.setPolymorphicName("MsJDBCElement");
        expected.add(httpProtocol);
        expected.add(jdbcProtocol);
        Assertions.assertEquals(protocols, expected);
    }
}
