package io.metersphere.system.controller;

import io.metersphere.plugin.platform.api.AbstractPlatformPlugin;
import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.param.ServiceIntegrationUpdateRequestDefinition;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.ServiceIntegrationDTO;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.PluginService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_ENABLE;
import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_PERMISSION;
import static io.metersphere.sdk.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.SERVICE_INTEGRATION_EXIST;
import static io.metersphere.system.service.ServiceIntegrationService.PLUGIN_IMAGE_GET_PATH;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author jianxing
 * @date : 2023-8-9
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceIntegrationControllerTests extends BaseTest {
    private static final String BASE_PATH = "/service/integration/";
    private static final String LIST = "/list/{0}";
    private static final String VALIDATE_GET = "/validate/{0}";
    private static final String VALIDATE_POST = "/validate/{0}";
    private static final String SCRIPT_GET = "/script/{0}";
    private static ServiceIntegration addServiceIntegration;
    private static Plugin plugin;
    private static Organization defaultOrg;

    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PluginService pluginService;
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private MockServerClient mockServerClient;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        defaultOrg = organizationService.getDefault();
        // @@请求成功, 校验空数据，请求是否正常
        this.requestGetWithOkAndReturn(LIST, defaultOrg.getId());
    }


    @Test
    @Order(1)
    public void add() throws Exception {
        plugin = addPlugin();

        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));

        // @@请求成功
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setEnable(false);
        request.setPluginId(plugin.getId());
        request.setConfiguration(integrationConfigMap);
        request.setOrganizationId(defaultOrg.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        // 校验请求成功数据
        ServiceIntegration resultData = getResultData(mvcResult, ServiceIntegration.class);
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(resultData.getId());
        Assertions.assertEquals(JSON.parseMap(new String(serviceIntegration.getConfiguration())), integrationConfigMap);
        Assertions.assertEquals(serviceIntegration.getEnable(), request.getEnable());
        Assertions.assertEquals(serviceIntegration.getPluginId(), request.getPluginId());
        Assertions.assertEquals(serviceIntegration.getOrganizationId(), request.getOrganizationId());
        this.addServiceIntegration = serviceIntegration;

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), SERVICE_INTEGRATION_EXIST);

        // @@校验插件禁用
        setPluginEnable(request.getPluginId(), false);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PLUGIN_ENABLE);
        setPluginEnable(request.getPluginId(), true);

        // @@校验权限
        setPluginGlobal(request.getPluginId(), false);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PLUGIN_PERMISSION);
        setPluginGlobal(request.getPluginId(), true);

        // @@校验日志
        checkLog(this.addServiceIntegration.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ServiceIntegrationUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));

        // @@请求成功
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setId(addServiceIntegration.getId());
        request.setEnable(false);
        request.setPluginId(plugin.getId());
        request.setConfiguration(integrationConfigMap);
        request.setOrganizationId(defaultOrg.getId());
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(request.getId());
        Assertions.assertEquals(JSON.parseMap(new String(serviceIntegration.getConfiguration())), integrationConfigMap);
        Assertions.assertEquals(serviceIntegration.getEnable(), request.getEnable());
        Assertions.assertEquals(serviceIntegration.getPluginId(), request.getPluginId());
        // 验证组织修改无效
        Assertions.assertEquals(serviceIntegration.getOrganizationId(), defaultOrg.getId());

        // 提升覆盖率
        request.setConfiguration(null);
        this.requestPostWithOk(DEFAULT_UPDATE, request);

        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);

        // @@校验插件禁用
        setPluginEnable(request.getPluginId(), false);
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), PLUGIN_ENABLE);
        setPluginEnable(request.getPluginId(), true);

        // @@校验权限
        setPluginGlobal(request.getPluginId(), false);
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), PLUGIN_PERMISSION);
        setPluginGlobal(request.getPluginId(), true);

        // @@校验 NOT_FOUND 异常
        request.setId("1111");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), NOT_FOUND);

        // @@异常参数校验
        updatedGroupParamValidateTest(ServiceIntegrationUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    public void list() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(LIST, defaultOrg.getId());
        // 校验请求成功数据
        List<ServiceIntegrationDTO> serviceIntegrationList = getResultDataArray(mvcResult, ServiceIntegrationDTO.class);
        ServiceIntegrationDTO serviceIntegrationDTO = serviceIntegrationList.get(0);
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(serviceIntegrationDTO.getId());
        Assertions.assertEquals(JSON.parseMap(new String(serviceIntegration.getConfiguration())),
                serviceIntegrationDTO.getConfiguration());
        Assertions.assertEquals(serviceIntegration.getEnable(), serviceIntegrationDTO.getEnable());
        Assertions.assertEquals(serviceIntegration.getPluginId(), serviceIntegrationDTO.getPluginId());
        AbstractPlatformPlugin msPluginInstance = (AbstractPlatformPlugin) pluginLoadService.getPluginWrapper(plugin.getId()).getPlugin();
        Assertions.assertEquals(serviceIntegrationDTO.getDescription(), msPluginInstance.getDescription());
        Assertions.assertEquals(serviceIntegrationDTO.getOrganizationId(), defaultOrg.getId());
        Assertions.assertEquals(serviceIntegrationDTO.getTitle(), msPluginInstance.getName());
        Assertions.assertEquals(serviceIntegrationDTO.getConfig(), true);
        Assertions.assertEquals(serviceIntegrationDTO.getLogo(),
                String.format(PLUGIN_IMAGE_GET_PATH, plugin.getId(), msPluginInstance.getLogo()));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ, LIST, defaultOrg.getId());
    }

    @Test
    @Order(4)
    public void validateGet() throws Exception {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/myself"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"self\"")
                );

        // @@请求成功
        this.requestGetWithOk(VALIDATE_GET, addServiceIntegration.getId());

        // @@校验插件禁用
        setPluginEnable(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestGet(VALIDATE_GET, addServiceIntegration.getId()), PLUGIN_ENABLE);
        setPluginEnable(addServiceIntegration.getPluginId(), true);

        // @@校验权限
        setPluginGlobal(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestGet(VALIDATE_GET, addServiceIntegration.getId()), PLUGIN_PERMISSION);
        setPluginGlobal(addServiceIntegration.getPluginId(), true);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(VALIDATE_GET, "1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, VALIDATE_GET, addServiceIntegration.getId());
    }

    @Test
    @Order(5)
    public void validatePost() throws Exception {
        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));
        // @@请求成功
        this.requestPostWithOk(VALIDATE_POST, integrationConfigMap, plugin.getId());

        // @@校验插件禁用
        setPluginEnable(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestPost(VALIDATE_POST, integrationConfigMap, plugin.getId()), PLUGIN_ENABLE);
        setPluginEnable(addServiceIntegration.getPluginId(), true);

        // @@校验权限
        setPluginGlobal(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestPost(VALIDATE_POST, integrationConfigMap, plugin.getId()), PLUGIN_PERMISSION);
        setPluginGlobal(addServiceIntegration.getPluginId(), true);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestPost(VALIDATE_POST, integrationConfigMap, "1111"), NOT_FOUND);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, VALIDATE_POST, integrationConfigMap, plugin.getId());
    }

    @Test
    @Order(6)
    public void getPluginScript() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(SCRIPT_GET, plugin.getId());
        // 校验请求成功数据
        Assertions.assertTrue(StringUtils.isNotBlank(mvcResult.getResponse().getContentAsString()));
        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(SCRIPT_GET, "1111"), NOT_FOUND);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ, SCRIPT_GET, plugin.getId());
    }

    @Test
    public void delete() throws Exception {

        // @@校验插件禁用
        setPluginEnable(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addServiceIntegration.getId()), PLUGIN_ENABLE);
        setPluginEnable(addServiceIntegration.getPluginId(), true);

        // @@校验权限
        setPluginGlobal(addServiceIntegration.getPluginId(), false);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addServiceIntegration.getId()), PLUGIN_PERMISSION);
        setPluginGlobal(addServiceIntegration.getPluginId(), true);

        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addServiceIntegration.getId());
        // 校验请求成功数据
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(addServiceIntegration.getId());
        Assertions.assertNull(serviceIntegration);

        // 清理插件
        deletePlugin();

        // @@校验日志
        checkLog(addServiceIntegration.getId(), OperationLogType.DELETE);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_DELETE, DEFAULT_DELETE, addServiceIntegration.getId());
    }


    /**
     * 添加插件，供测试使用
     *
     * @return
     * @throws Exception
     */
    public Plugin addPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-jira-plugin-3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

    public void setPluginEnable(String pluginId, boolean enabled) {
        Plugin plugin = new Plugin();
        plugin.setId(pluginId);
        plugin.setEnable(enabled);
        pluginMapper.updateByPrimaryKeySelective(plugin);
    }

    public void setPluginGlobal(String pluginId, boolean global) {
        Plugin plugin = new Plugin();
        plugin.setId(pluginId);
        plugin.setGlobal(global);
        pluginMapper.updateByPrimaryKeySelective(plugin);
    }

    /**
     * 删除插件
     * @throws Exception
     */
    public void deletePlugin() {
        pluginService.delete(plugin.getId());
    }

    @Getter
    @Setter
    public class JiraIntegrationConfig {
        private String account;
        private String password;
        private String token;
        private String authType;
        private String address;
        private String version;
    }
}