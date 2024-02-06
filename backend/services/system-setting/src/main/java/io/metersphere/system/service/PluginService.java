package io.metersphere.system.service;


import io.metersphere.plugin.api.spi.AbstractApiPlugin;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.dto.request.PluginOptionsRequest;
import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.plugin.sdk.spi.QuotaPlugin;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.handler.result.CommonResultCode;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.dto.PluginNotifiedDTO;
import io.metersphere.system.dto.request.PlatformOptionRequest;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.ExtPluginMapper;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.util.*;

import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_EXIST;
import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_PARSE_ERROR;

/**
 * @author jianxing
 * @date : 2023-7-13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private ExtPluginMapper extPluginMapper;
    @Resource
    private PluginScriptService pluginScriptService;
    @Resource
    private PluginOrganizationService pluginOrganizationService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private JdbcDriverPluginService jdbcDriverPluginService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private PlatformPluginService platformPluginService;

    public List<PluginDTO> list() {
        List<PluginDTO> plugins = extPluginMapper.getPlugins();
        List<String> pluginIds = plugins.stream().map(Plugin::getId).toList();
        Map<String, List<OptionDTO>> scripteMap = pluginScriptService.getScripteMap(pluginIds);
        Map<String, List<OptionDTO>> orgMap = pluginOrganizationService.getOrgMap(pluginIds);

        // 获取用户ID和名称的映射
        List<String> userIds = plugins.stream().map(PluginDTO::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);

        plugins.forEach(plugin -> {
            List<OptionDTO> pluginForms = scripteMap.get(plugin.getId());
            List<OptionDTO> organizations = orgMap.get(plugin.getId());
            plugin.setPluginForms(pluginForms == null ? new ArrayList<>(0) : pluginForms);
            plugin.setOrganizations(organizations == null ? new ArrayList<>(0) : organizations);
            plugin.setCreateUser(userNameMap.get(plugin.getCreateUser()));
        });

        return plugins;
    }

    public Plugin get(String id) {
        return pluginMapper.selectByPrimaryKey(id);
    }

    public Plugin add(PluginUpdateRequest request, MultipartFile file) {
        String id = null;
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setFileName(file.getOriginalFilename());
        plugin.setCreateTime(System.currentTimeMillis());
        plugin.setUpdateTime(System.currentTimeMillis());
        // 如果没有，默认设置为 true
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));

        // 校验重名
        checkPluginAddExist(plugin);

        try {
            // 上传插件到本地文件系统
            pluginLoadService.uploadPlugin2Local(file);

            // 从文件系统中加载插件
            id = pluginLoadService.loadPlugin(file.getOriginalFilename());
            plugin.setId(id);

            List<Driver> extensions = pluginLoadService.getMsPluginManager().getExtensions(Driver.class, id);

            if (CollectionUtils.isNotEmpty(extensions)) {
                plugin = jdbcDriverPluginService.wrapperPlugin(plugin);
                plugin.setPluginId(file.getOriginalFilename());
            } else {

                plugin = wrapperPlugin(id, plugin);

                // 非数据库驱动插件，解析脚本和插件信息
                List<String> frontendScript = pluginLoadService.getFrontendScripts(id);
                pluginScriptService.add(id, frontendScript);
            }

            // 保存插件和组织的关联关系
            if (!request.getGlobal()) {
                pluginOrganizationService.add(id, request.getOrganizationIds());
            }

            pluginMapper.insert(plugin);

            // 上传插件到对象存储
            pluginLoadService.uploadPlugin2Repository(file);

            // 通知其他节点加载插件
            notifiedPluginAdd(id, plugin.getFileName());
        } catch (Throwable e) {
            // 删除插件
            pluginLoadService.unloadPlugin(id);
            pluginLoadService.deletePluginFile(file.getOriginalFilename());
            if (e instanceof MSException) {
                throw e;
            }
            throw new MSException(PLUGIN_PARSE_ERROR, e);
        }
        return plugin;
    }

    public Plugin wrapperPlugin(String id, Plugin plugin) {
        PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(id);
        PluginDescriptor descriptor = pluginWrapper.getDescriptor();
        MsPlugin msPlugin = (MsPlugin) pluginWrapper.getPlugin();
        if (msPlugin instanceof AbstractApiPlugin) {
            plugin.setScenario(PluginScenarioType.API_PROTOCOL.name());
        } else if (msPlugin instanceof AbstractPlatformPlugin) {
            plugin.setScenario(PluginScenarioType.PLATFORM.name());
        }else if(msPlugin instanceof QuotaPlugin){
            plugin.setScenario(PluginScenarioType.QUOTA.name());
        }
        plugin.setXpack(msPlugin.isXpack());
        plugin.setPluginId(descriptor.getPluginId() + "-" + descriptor.getVersion());
        return plugin;
    }

    public Plugin checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(pluginMapper.selectByPrimaryKey(id), "permission.system_plugin.name");
    }

    private void checkPluginAddExist(Plugin plugin) {
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andNameEqualTo(plugin.getName());
        PluginExample.Criteria criteria = example.createCriteria()
                .andFileNameEqualTo(plugin.getFileName());
        example.or(criteria);
        if (CollectionUtils.isNotEmpty(pluginMapper.selectByExample(example))) {
            throw new MSException(PLUGIN_EXIST);
        }
    }

    /**
     * 通知其他节点加载插件
     * 这里需要传一下 fileName，事务未提交，查询不到文件名
     *
     * @param pluginId
     * @param fileName
     */
    public void notifiedPluginAdd(String pluginId, String fileName) {
        notifiedPluginOperate(pluginId, fileName, KafkaTopicConstants.TYPE.ADD);
    }

    public void notifiedPluginOperate(String pluginId, String fileName, String operate) {
        PluginNotifiedDTO pluginNotifiedDTO = new PluginNotifiedDTO();
        pluginNotifiedDTO.setOperate(operate);
        pluginNotifiedDTO.setPluginId(pluginId);
        pluginNotifiedDTO.setFileName(fileName);
        kafkaTemplate.send(KafkaTopicConstants.PLUGIN, JSON.toJSONString(pluginNotifiedDTO));
    }

    /**
     * 通知其他节点卸载插件
     *
     * @param pluginId
     */
    public void notifiedPluginDelete(String pluginId, String fileName) {
        notifiedPluginOperate(pluginId, fileName, KafkaTopicConstants.TYPE.DELETE);
    }

    public Plugin update(PluginUpdateRequest request) {
        checkResourceExist(request.getId());
        request.setCreateUser(null);
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setCreateTime(null);
        plugin.setUpdateTime(System.currentTimeMillis());
        // 校验重名
        checkPluginUpdateExist(plugin);
        pluginMapper.updateByPrimaryKeySelective(plugin);
        if (BooleanUtils.isTrue(request.getGlobal())) {
            // 全局插件，删除和组织的关联关系
            request.setOrganizationIds(new ArrayList<>(0));
        }
        pluginOrganizationService.update(plugin.getId(), request.getOrganizationIds());
        return pluginMapper.selectByPrimaryKey(request.getId());
    }

    private void checkPluginUpdateExist(Plugin plugin) {
        if (StringUtils.isBlank(plugin.getName())) {
            return;
        }
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andIdNotEqualTo(plugin.getId())
                .andNameEqualTo(plugin.getName());
        if (CollectionUtils.isNotEmpty(pluginMapper.selectByExample(example))) {
            throw new MSException(PLUGIN_EXIST);
        }
    }

    public void delete(String id) {
        checkResourceExist(id);
        Plugin plugin = pluginMapper.selectByPrimaryKey(id);
        pluginMapper.deleteByPrimaryKey(id);
        // 删除插件脚本
        pluginScriptService.deleteByPluginId(id);
        // 删除和组织的关联关系
        pluginOrganizationService.deleteByPluginId(id);
        // 删除和卸载插件
        pluginLoadService.unloadPlugin(id);
        pluginLoadService.deletePluginFile(plugin.getFileName());
        notifiedPluginDelete(id, plugin.getFileName());
    }

    public String getScript(String pluginId, String scriptId) {
        return pluginScriptService.getScriptContent(pluginId, scriptId);
    }

    public void getPluginImg(String pluginId, String filePath, HttpServletResponse response) throws IOException {
        validateImageFileName(filePath);
        InputStream inputStream = pluginLoadService.getResourceAsStream(pluginId, filePath);
        writeImage(filePath, inputStream, response);
    }

    public void validateImageFileName(String filename) {
        Set<String> imgSuffix = new HashSet<>() {{
            add("jpg");
            add("png");
            add("gif");
            add("jpeg");
        }};
        if (!imgSuffix.contains(StringUtils.substringAfterLast(filename, "."))) {
            throw new MSException(CommonResultCode.FILE_NAME_ILLEGAL);
        }
    }

    public void writeImage(String filePath, InputStream in, HttpServletResponse response) throws IOException {
        response.setContentType("image/" + StringUtils.substringAfterLast(filePath, "."));
        try (OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    public List<SelectOption> getPluginOptions(PlatformOptionRequest request) {
        try {
        Platform platform = platformPluginService.getPlatform(request.getPluginId(), request.getOrganizationId());
        PluginOptionsRequest optionsRequest = new PluginOptionsRequest();
        optionsRequest.setOptionMethod(request.getOptionMethod());
        optionsRequest.setProjectConfig(request.getProjectConfig());
        return platform.getPluginOptions(optionsRequest);
        }catch (Exception e){
            return new ArrayList<>();
        }
    }
}