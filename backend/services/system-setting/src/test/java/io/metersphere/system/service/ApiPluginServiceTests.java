package io.metersphere.system.service;

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
 * @CreateTime: 2023-11-08  16:34
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiPluginServiceTests extends BaseTest {
    @Resource
    private BaseApiPluginTestService baseApiPluginTestService;
    @Resource
    private ApiPluginService apiPluginService;

    @Test
    public void getProtocols() throws Exception {
        baseApiPluginTestService.addJdbcPlugin();

        // 校验数据是否正确
        List<ProtocolDTO> protocols = apiPluginService.getProtocols(this.DEFAULT_ORGANIZATION_ID);
        List expected = new ArrayList<ProtocolDTO>();
        ProtocolDTO jdbcProtocol = new ProtocolDTO();
        jdbcProtocol.setProtocol("JDBC");
        jdbcProtocol.setPolymorphicName("MsJDBCElement");
        jdbcProtocol.setPluginId("jdbc");
        expected.add(jdbcProtocol);
        Assertions.assertEquals(protocols, expected);
    }
}
