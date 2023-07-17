package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.io.File;

/**
 * @author jianxing
 * @date : 2023-7-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PluginControllerTests extends BaseTest {
    private static final String BASE_PATH = "/plugin/";
    private static final String SCRIPT_GET = "script/get/{0}/{1}";
    @Resource
    private PluginMapper pluginMapper;
    private static Plugin addPlugin;
    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void list() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_LIST)
                .andReturn();
//        List<Plugin> pluginList = getResultDataArray(mvcResult, Plugin.class);
        // todo 校验数据是否正确
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_READ, DEFAULT_LIST);
    }

    @Test
    @Order(0)
    public void add() throws Exception {
        // @@请求成功
        PluginUpdateRequest request = new PluginUpdateRequest();
        request.setName("test");
        request.setDescription("test desc");
        request.setScenario(PluginScenarioType.PAI.name());
        MultiValueMap<String, Object> multiValueMap = getDefaultMultiPartParam(request,
                new File("src/test/resources/application.properties"));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(DEFAULT_ADD, multiValueMap);
        Plugin resultData = getResultData(mvcResult, Plugin.class);
        Plugin plugin = pluginMapper.selectByPrimaryKey(resultData.getId());
        this.addPlugin = plugin;
        // todo 校验请求成功数据
        // @@校验日志
        // checkLog(this.addPlugin.getId(), OperationLogType.ADD);
        // @@异常参数校验
//        createdGroupParamValidateTest(PluginUpdateRequestDefinition.class, ADD);
        // @@校验权限
        requestMultipartPermissionTest(PermissionConstants.SYSTEM_PLUGIN_ADD, DEFAULT_ADD, multiValueMap);
    }

    @Test
    @Order(1)
    public void get() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_GET, this.addPlugin.getId())
                .andReturn();
//        Plugin plugin = getResultData(mvcResult, Plugin.class);
        // todo 校验数据是否正确
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_READ, DEFAULT_GET, this.addPlugin.getId());
    }

    @Test
    @Order(2)
    public void getScript() throws Exception {
        // @@请求成功
        this.requestGetWithOk(SCRIPT_GET, this.addPlugin.getId(), "script id")
                .andReturn();
//        Plugin plugin = getResultData(mvcResult, Plugin.class);
        // todo 校验数据是否正确
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_READ, SCRIPT_GET, this.addPlugin.getId(), "script id");
    }


    @Test
    public void update() throws Exception {
        // @@请求成功
        PluginUpdateRequest request = new PluginUpdateRequest();
        request.setId(addPlugin.getId());
        request.setName("test update");

        MultiValueMap<String, Object> multiValueMap = getDefaultMultiPartParam(request,
                new File("src/test/resources/application.properties"));
        this.requestMultipartWithOk(DEFAULT_UPDATE, multiValueMap);
        // 校验请求成功数据
//        Plugin plugin = pluginMapper.selectByPrimaryKey(request.getId());
        // todo 校验请求成功数据
        // @@校验日志
        //  checkLog(request.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
//        updatedGroupParamValidateTest(PluginUpdateRequestDefinition.class, UPDATE);
        // @@校验权限
        requestMultipartPermissionTest(PermissionConstants.SYSTEM_PLUGIN_UPDATE, DEFAULT_UPDATE, multiValueMap);
    }

    @Test
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addPlugin.getId());
        // todo 校验请求成功数据
        // @@校验日志
        // checkLog(addPlugin.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_DELETE, DEFAULT_DELETE, addPlugin.getId());
    }
}