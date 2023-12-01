package io.metersphere.api.controller;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.metersphere.sdk.util.ApiDataUtils;
import io.metersphere.plugin.api.dto.TestElementDTO;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.dto.api.request.logic.controller.MsLoopController;
import io.metersphere.system.base.BaseApiPluginTestService;
import io.metersphere.system.service.PluginLoadService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class PluginSubTypeTests {

    @Resource
    private BaseApiPluginTestService baseApiPluginTestService;
    @Resource
    private PluginLoadService pluginLoadService;


    @Test
    @Order(0)
    public void resolverTest() throws Exception {
        List<NamedType> namedTypes = new LinkedList<>();
        namedTypes.add(new NamedType(MsLoopController.class, MsLoopController.class.getSimpleName()));
        ApiDataUtils.setResolver(namedTypes);
    }

    @Test
    @Order(1)
    public void newPluginSubTypeTest() throws Exception {
        MsLoopController loopController = new MsLoopController();
        loopController.setName("测试loopController");
        String json = ApiDataUtils.toJSONString(loopController);

        TestElementDTO testElementDTO = ApiDataUtils.parseObject(json, TestElementDTO.class);
        Assertions.assertNotNull(testElementDTO);

    }

    @Test
    @Order(2)
    public void jdbcPluginSubTypeTest() throws Exception {
        // 上传 jdbc 插件
        baseApiPluginTestService.addJdbcPlugin();
        List<Class<? extends MsTestElement>> extensionClasses =
                pluginLoadService.getMsPluginManager().getExtensionClasses(MsTestElement.class);

        // 注册序列化类
        extensionClasses.forEach(ApiDataUtils::setResolver);

        String jdbcJson = """
                {
                  "polymorphicName": "MsJDBCElement",
                  "test": "测试MsJDBCElement"
                }
                """;
        AbstractMsTestElement testElementDTO = ApiDataUtils.parseObject(jdbcJson, AbstractMsTestElement.class);
        Assertions.assertNotNull(testElementDTO);
    }

    @Test
    @Order(3)
    public void testApiDataUtils() throws Exception {
        // 校验异常，增加覆盖率
        Assertions.assertTrue(isFuncSuccess((v) -> {
            InputStream in = null;
            ApiDataUtils.parseObject(in, AbstractMsTestElement.class);
        }));
        Assertions.assertTrue(isFuncSuccess((v) -> ApiDataUtils.parseObject("{")));
        Assertions.assertTrue(isFuncSuccess((v) -> ApiDataUtils.parseArray(null, AbstractMsTestElement.class)));

        ApiDataUtils.setResolver(MsHTTPElement.class);
        // 检验 parseArray
        String msHttpJson = """
                [{
                  "polymorphicName": "MsHTTPElement",
                  "test": "测试MsHTTPElement"
                }]
                """;
        ApiDataUtils.parseArray(msHttpJson, AbstractMsTestElement.class);
    }

    /**
     * 判断函数是否抛出异常
     *
     * @param func
     * @return
     * @throws Exception
     */
    public boolean isFuncSuccess(Consumer func) {
        try {
            func.accept("");
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}
