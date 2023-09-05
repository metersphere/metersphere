package io.metersphere.system.service;


import io.metersphere.plugin.sdk.api.MsPlugin;
import io.metersphere.sdk.constants.KafkaPluginTopicType;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.controller.handler.result.CommonResultCode;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserService;
import io.metersphere.sdk.service.JdbcDriverPluginService;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.ServiceUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.mapper.ExtPluginMapper;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_TYPE_EXIST;

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
    JdbcDriverPluginService jdbcDriverPluginService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private BaseUserService baseUserService;

    public List<PluginDTO> list() {
        List<PluginDTO> plugins = extPluginMapper.getPlugins();
        List<String> pluginIds = plugins.stream().map(Plugin::getId).toList();
        Map<String, List<OptionDTO>> scripteMap = pluginScriptService.getScripteMap(pluginIds);
        Map<String, List<OptionDTO>> orgMap = pluginOrganizationService.getOrgMap(pluginIds);

        // 获取用户ID和名称的映射
        List<String> userIds = plugins.stream().map(PluginDTO::getCreateUser).toList();
        Map<String, String> userNameMap = baseUserService.getUserNameMap(userIds);

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
        String id = UUID.randomUUID().toString();
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setId(id);
        plugin.setFileName(file.getOriginalFilename());
        plugin.setCreateTime(System.currentTimeMillis());
        plugin.setUpdateTime(System.currentTimeMillis());
        // 如果没有，默认设置为 true
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));

        // 校验重名
        checkPluginAddExist(plugin);

        try {
            // 加载插件
            pluginLoadService.loadPlugin(id, file);
            // 上传插件
            pluginLoadService.uploadPlugin(id, file);

            if (jdbcDriverPluginService.isJdbcDriver(id)) {
                Class implClass = pluginLoadService.getImplClass(id, Driver.class);
                // mysql 已经内置了依赖，不允许上传
                if (implClass.getName().startsWith("com.mysql")) {
                    throw new MSException(PLUGIN_TYPE_EXIST);
                }
                plugin.setScenario(PluginScenarioType.JDBC_DRIVER.name());
                plugin.setXpack(false);
                plugin.setPluginId(file.getOriginalFilename());
            } else {
                // 非数据库驱动插件，解析脚本和插件信息
                // 获取插件前端配置脚本
                List<String> frontendScript = pluginLoadService.getFrontendScripts(id);

                MsPlugin msPlugin = pluginLoadService.getMsPluginInstance(id);
                plugin.setScenario(msPlugin.getType());
                plugin.setXpack(msPlugin.isXpack());
                plugin.setPluginId(msPlugin.getPluginId());

                // 校验插件类型是否重复
                checkPluginKeyExist(id, msPlugin.getKey());

                // 保存插件脚本
                pluginScriptService.add(id, frontendScript);
            }

            // 保存插件和组织的关联关系
            if (!request.getGlobal()) {
                pluginOrganizationService.add(id, request.getOrganizationIds());
            }

            pluginMapper.insert(plugin);

            // 通知其他节点加载插件
            notifiedPluginAdd(id, plugin.getFileName());
        } catch (Exception e) {
            // 删除插件
            pluginLoadService.deletePlugin(id);
            throw e;
        }
        return plugin;
    }

    private void checkPluginKeyExist(String pluginId, String pluginKey) {
        if (pluginLoadService.hasPluginKey(pluginId, pluginKey)) {
            throw new MSException(PLUGIN_TYPE_EXIST);
        }
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
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PLUGIN, String.format("%s:%s:%s", KafkaPluginTopicType.ADD, pluginId, fileName));
    }

    /**
     * 通知其他节点卸载插件
     *
     * @param pluginId
     */
    public void notifiedPluginDelete(String pluginId) {
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PLUGIN, String.format("%s:%s", KafkaPluginTopicType.DELETE, pluginId));
    }

    public Plugin update(PluginUpdateRequest request) {
        checkResourceExist(request.getId());
        request.setCreateUser(null);
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setCreateTime(null);
        plugin.setUpdateTime(null);
        // 校验重名
        checkPluginUpdateExist(plugin);
        pluginMapper.updateByPrimaryKeySelective(plugin);
        if (BooleanUtils.isTrue(request.getGlobal())) {
            // 全局插件，删除和组织的关联关系
            request.setOrganizationIds(new ArrayList<>(0));
        }
        pluginOrganizationService.update(plugin.getId(), request.getOrganizationIds());
        return plugin;
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
        pluginMapper.deleteByPrimaryKey(id);
        // 删除插件脚本
        pluginScriptService.deleteByPluginId(id);
        // 删除和组织的关联关系
        pluginOrganizationService.deleteByPluginId(id);
        // 删除和卸载插件
        pluginLoadService.deletePlugin(id);
        notifiedPluginDelete(id);
    }

    public String getScript(String pluginId, String scriptId) {
        return pluginScriptService.get(pluginId, scriptId);
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
}