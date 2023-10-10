package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.BaseUserService;
import io.metersphere.system.service.JdbcDriverPluginService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.param.PluginUpdateRequestDefinition;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.PluginOrganizationMapper;
import io.metersphere.system.mapper.PluginScriptMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import io.metersphere.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.CommonResultCode.FILE_NAME_ILLEGAL;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jianxing
 * @date : 2023-7-14
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PluginControllerTests extends BaseTest {
    private static final String BASE_PATH = "/plugin/";
    private static final String SCRIPT_GET = "script/get/{0}/{1}";
    private static final String PLUGIN_IMAGE = "/image/{0}?imagePath={1}";
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private PluginOrganizationMapper pluginOrganizationMapper;
    @Resource
    private PluginScriptMapper pluginScriptMapper;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private JdbcDriverPluginService jdbcDriverPluginService;
    private static Plugin addPlugin;
    private static Plugin anotherAddPlugin;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        // @@没有数据是校验是否成功
        this.requestGetWithOkAndReturn(DEFAULT_LIST);
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        // @@请求成功
        PluginUpdateRequest request = new PluginUpdateRequest();
        OrganizationDTO org = organizationService.getDefault();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-mqtt-plugin-3.x.jar")
                        .getPath()
        );

        request.setName("test");
        request.setDescription("test desc");
        request.setGlobal(true);
        request.setEnable(false);
        request.setOrganizationIds(Arrays.asList(org.getId()));
        MultiValueMap<String, Object> multiValueMap = getDefaultMultiPartParam(request, jarFile);

        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(DEFAULT_ADD, multiValueMap);
        // 校验数据是否正确
        Plugin resultData = getResultData(mvcResult, Plugin.class);
        Plugin plugin = pluginMapper.selectByPrimaryKey(resultData.getId());
        Assertions.assertEquals(plugin.getName(), request.getName());
        Assertions.assertEquals(plugin.getDescription(), request.getDescription());
        Assertions.assertEquals(plugin.getEnable(), request.getEnable());
        Assertions.assertEquals(plugin.getGlobal(), request.getGlobal());
        Assertions.assertEquals(plugin.getXpack(), false);
        Assertions.assertEquals(plugin.getFileName(), jarFile.getName());
        Assertions.assertEquals(plugin.getScenario(), PluginScenarioType.API_PROTOCOL.name());
        Assertions.assertEquals(new ArrayList<>(0), getOrgIdsByPlugId(plugin.getId()));
        Assertions.assertEquals(Arrays.asList("connect", "disconnect", "pub", "sub"), getScriptIdsByPlugId(plugin.getId()));
        addPlugin = plugin;

        // 增加覆盖率
        this.requestGetWithOkAndReturn(DEFAULT_LIST);

        // 校验 global 为 tru e时，organizationIds 为空
        request.setGlobal(false);
        request.setEnable(true);
        request.setName("test2");
        request.setOrganizationIds(Arrays.asList(org.getId()));
        File anotherJarFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-jira-plugin-3.x.jar")
                        .getPath()
        );
        MvcResult antoherMvcResult = this.requestMultipartWithOkAndReturn(DEFAULT_ADD,
                getDefaultMultiPartParam(request, anotherJarFile));
        Plugin antoherPlugin = pluginMapper.selectByPrimaryKey(getResultData(antoherMvcResult, Plugin.class).getId());
        Assertions.assertEquals(antoherPlugin.getEnable(), request.getEnable());
        Assertions.assertEquals(antoherPlugin.getGlobal(), request.getGlobal());
        Assertions.assertEquals(Arrays.asList(org.getId()), getOrgIdsByPlugId(antoherPlugin.getId()));
        anotherAddPlugin = antoherPlugin;
        addPlugin = plugin;

        // 校验数据库驱动上传成功
        request.setName("my-driver");
        request.setOrganizationIds(Arrays.asList(org.getId()));
        File myDriver = new File(
                this.getClass().getClassLoader().getResource("file/my-driver-1.0.jar")
                        .getPath()
        );
        this.requestMultipartWithOkAndReturn(DEFAULT_ADD,
                getDefaultMultiPartParam(request, myDriver));
        Assertions.assertEquals(jdbcDriverPluginService.getJdbcDriverClass(DEFAULT_ORGANIZATION_ID), Arrays.asList("io.jianxing.MyDriver", "com.mysql.cj.jdbc.Driver"));

        // @@重名校验异常
        // 校验插件名称重名
        assertErrorCode(this.requestMultipart(DEFAULT_ADD,
                getDefaultMultiPartParam(request, anotherJarFile)), PLUGIN_EXIST);

        // 校验文件名重名
        request.setName("test1");
        assertErrorCode(this.requestMultipart(DEFAULT_ADD,
                getDefaultMultiPartParam(request, jarFile)), PLUGIN_EXIST);


        // @@校验插件脚本解析失败
        // TODO 缺少有效jar包
       /* File scriptParseFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-plugin-script-parse-error.jar")
                        .getPath()
        );
        assertErrorCode(this.requestMultipart(DEFAULT_ADD,
                getDefaultMultiPartParam(request, scriptParseFile)), PLUGIN_SCRIPT_FORMAT);*/

        // @@校验插件脚本ID重复
        // TODO 缺少有效jar包
       /* File scriptIdRepeatFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-plugin-script-id-repeat-error.jar")
                        .getPath()
        );
        assertErrorCode(this.requestMultipart(DEFAULT_ADD,
                getDefaultMultiPartParam(request, scriptIdRepeatFile)), PLUGIN_SCRIPT_EXIST);     // @@校验插件脚本ID重复*/

        // @@校验日志
        checkLog(this.addPlugin.getId(), OperationLogType.ADD);
        // @@校验权限
        requestMultipartPermissionTest(PermissionConstants.SYSTEM_PLUGIN_ADD, DEFAULT_ADD, multiValueMap);
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        // @@请求成功
        PluginUpdateRequest request = new PluginUpdateRequest();
        OrganizationDTO org = organizationService.getDefault();
        request.setId(addPlugin.getId());
        request.setName("test update");
        request.setCreateUser("test update user");
        request.setDescription("test update desc");
        request.setEnable(true);
        request.setGlobal(true);
        request.setOrganizationIds(Arrays.asList(org.getId()));

        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        Plugin plugin = pluginMapper.selectByPrimaryKey(request.getId());
        Assertions.assertEquals(plugin.getName(), request.getName());
        Assertions.assertEquals(plugin.getDescription(), request.getDescription());
        Assertions.assertEquals(plugin.getEnable(), request.getEnable());
        Assertions.assertEquals(plugin.getGlobal(), request.getGlobal());
        Assertions.assertEquals(plugin.getXpack(), false);
        // 校验 global 为 true 时，organizationIds 为空
        Assertions.assertEquals(new ArrayList<>(0), getOrgIdsByPlugId(plugin.getId()));

        // 这些数据不能修改
        Assertions.assertEquals(plugin.getFileName(), addPlugin.getFileName());
        Assertions.assertEquals(plugin.getScenario(), addPlugin.getScenario());
        Assertions.assertEquals(plugin.getCreateUser(), addPlugin.getCreateUser());

        // 校验 global 为 false 时，organizationIds 数据
        request.setGlobal(false);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Assertions.assertEquals(Arrays.asList(org.getId()), getOrgIdsByPlugId(request.getId()));

        // 只修改启用禁用
        PluginUpdateRequest activeRequest = new PluginUpdateRequest();
        activeRequest.setId(request.getId());
        activeRequest.setEnable(true);
        this.requestPostWithOk(DEFAULT_UPDATE, activeRequest);

        // 校验组织为null，不修改关联关系
        request.setOrganizationIds(null);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Assertions.assertEquals(Arrays.asList(org.getId()), getOrgIdsByPlugId(request.getId()));

        // @@重名校验异常
        request.setName(anotherAddPlugin.getName());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), PLUGIN_EXIST);

        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);

        // @@校验 NOT_FOUND 异常
        request.setId("1111");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), NOT_FOUND);

        // @@异常参数校验
        updatedGroupParamValidateTest(PluginUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_PLUGIN_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    public void getScript() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(SCRIPT_GET, this.addPlugin.getId(), "connect").andReturn();
        // 校验数据是否正确
        Assertions.assertTrue(StringUtils.isNotBlank(getResultData(mvcResult, String.class)));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_READ, SCRIPT_GET, this.addPlugin.getId(), "connect");
    }

    @Test
    @Order(4)
    public void list() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_LIST);
        // 校验数据是否正确
        List<PluginDTO> pluginList = getResultDataArray(mvcResult, PluginDTO.class);
        Assertions.assertEquals(3, pluginList.size());

        // 获取数据库数据
        List<String> pluginIds = pluginList.stream().map(PluginDTO::getId).toList();
        PluginExample example = new PluginExample();
        example.createCriteria().andPluginIdNotIn(pluginIds);
        List<Plugin> dbPlugins = pluginMapper.selectByExample(example);
        List<String> userIds = dbPlugins.stream().map(Plugin::getCreateUser).toList();
        Map<String, String> userNameMap = baseUserService.getUserNameMap(userIds);
        Map<String, Plugin> dbPluginMap = dbPlugins.stream().collect(Collectors.toMap(Plugin::getId, i -> i));

        for (PluginDTO pluginDTO : pluginList) {
            Plugin comparePlugin = dbPluginMap.get(pluginDTO.getId());
            Plugin plugin = JSON.parseObject(JSON.toJSONString(pluginDTO), Plugin.class);
            List<String> scriptIds = pluginDTO.getPluginForms().stream().map(OptionDTO::getId).toList();
            comparePlugin.setCreateUser(userNameMap.get(comparePlugin.getCreateUser()));
            Assertions.assertEquals(plugin, comparePlugin);
            Assertions.assertEquals(scriptIds, getScriptIdsByPlugId(plugin.getId()));
            List<OptionDTO> orgList = Optional.ofNullable(pluginDTO.getOrganizations()).orElse(new ArrayList<>(0));
            Assertions.assertEquals(orgList.stream().map(OptionDTO::getId).toList(), getOrgIdsByPlugId(plugin.getId()));
        }

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_READ, DEFAULT_LIST);
    }

    @Test
    @Order(5)
    public void getPluginImg() throws Exception {
        // @@请求成功
        mockMvc.perform(getRequestBuilder(PLUGIN_IMAGE, anotherAddPlugin.getId(), "static/jira.jpg"))
                .andExpect(status().isOk());

        assertErrorCode(this.requestGet(PLUGIN_IMAGE, anotherAddPlugin.getId(), "static/jira.doc"), FILE_NAME_ILLEGAL);
    }

    @Test
    @Order(6)
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addPlugin.getId());
        // 校验请求成功数据
        Plugin plugin = pluginMapper.selectByPrimaryKey(addPlugin.getId());
        Assertions.assertNull(plugin);
        Assertions.assertEquals(new ArrayList<>(0), getOrgIdsByPlugId(addPlugin.getId()));
        Assertions.assertEquals(new ArrayList<>(0), getScriptIdsByPlugId(addPlugin.getId()));
        this.requestGetWithOk(DEFAULT_DELETE, anotherAddPlugin.getId());

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addPlugin.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PLUGIN_DELETE, DEFAULT_DELETE, addPlugin.getId());
    }

    private List<String> getOrgIdsByPlugId(String pluginId) {
        PluginOrganizationExample example = new PluginOrganizationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        return pluginOrganizationMapper.selectByExample(example)
                .stream()
                .map(PluginOrganization::getOrganizationId)
                .toList();
    }

    private List<String> getScriptIdsByPlugId(String pluginId) {
        PluginScriptExample example = new PluginScriptExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        return pluginScriptMapper.selectByExample(example)
                .stream()
                .map(PluginScript::getScriptId)
                .toList();
    }
}