package io.metersphere.system.base;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.dto.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.service.ServiceIntegrationService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-20  11:32
 */
@Service
public class BasePluginTestService {

    @Resource
    private PluginService pluginService;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;
    @Resource
    private ServiceIntegrationService serviceIntegrationService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private PluginMapper pluginMapper;
    private static Plugin jiraPlugin;
    private static ServiceIntegration serviceIntegration;


    /**
     * 添加插件，供测试使用
     *
     * @return
     * @throws Exception
     */
    public synchronized Plugin addJiraPlugin() throws Exception {
        if (hasJiraPlugin()) {
            return jiraPlugin;
        }
        PluginUpdateRequest request = new PluginUpdateRequest();
        FileUtils.copyInputStreamToFile(this.getClass().getClassLoader().getResource("file/metersphere-jira-plugin-3.x.jar").openStream(), new File(FileUtils.getTempDirectoryPath()+"/metersphere-jira-plugin-3.x.jar"));
        File jarFile = new File(FileUtils.getTempDirectoryPath()+"/metersphere-jira-plugin-3.x.jar");
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        jiraPlugin = pluginService.add(request, mockMultipartFile);
        return jiraPlugin;
    }

    /**
     * 添加服务集成信息
     * @return
     * @throws Exception
     */
    public ServiceIntegration addServiceIntegration(String orgId) {
        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));

        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setEnable(true);
        request.setPluginId(jiraPlugin.getId());
        request.setConfiguration(integrationConfigMap);
        request.setOrganizationId(orgId);
        serviceIntegration = serviceIntegrationService.add(request);
        return serviceIntegration;
    }

    public synchronized Plugin getJiraPlugin() throws Exception {
        if (!hasJiraPlugin()) {
            return this.addJiraPlugin();
        }
        return jiraPlugin;
    }

    public boolean hasJiraPlugin() {
        if (jiraPlugin != null) {
            return true;
        }
        jiraPlugin = pluginMapper.selectByPrimaryKey("jira");
        return jiraPlugin != null;
    }

    public synchronized void deleteJiraPlugin() {
        if (jiraPlugin != null) {
            pluginService.delete(jiraPlugin.getId());
            jiraPlugin = null;
        }
    }

    public void enableProjectBugConfig(String defaultProjectId) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(defaultProjectId);
        projectApplication.setType(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name());
        projectApplication.setTypeValue(BooleanUtils.toStringTrueFalse(true));
        createOrUpdateConfig(projectApplication);
    }

    public void createOrUpdateConfig(ProjectApplication application) {
        String type = application.getType();
        String projectId = application.getProjectId();
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
            projectApplicationMapper.updateByExample(application, example);
        } else {
            projectApplicationMapper.insertSelective(application);
        }
    }


    @Getter
    @Setter
    public static class JiraIntegrationConfig {
        private String account;
        private String password;
        private String token;
        private String authType;
        private String address;
        private String version;
    }
}
