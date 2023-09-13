package io.metersphere.sdk.service.environment;

import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentBlobExample;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.environment.EnvironmentConfig;
import io.metersphere.sdk.dto.environment.EnvironmentRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.domain.PluginOrganization;
import io.metersphere.system.domain.PluginOrganizationExample;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.PluginOrganizationMapper;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Driver;
import java.util.*;


@Service
@Transactional
public class EnvironmentService {

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PluginOrganizationMapper pluginOrganizationMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private MinioRepository minioRepository;

    public Map<String, String> getDriverOptions(String organizationId) {
        Map<String, String> pluginDriverClassNames = new HashMap<>();
        PluginExample example = new PluginExample();
        example.createCriteria().andScenarioEqualTo(PluginScenarioType.JDBC_DRIVER.name()).andEnableEqualTo(true);
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        plugins.forEach(plugin -> {
            if (BooleanUtils.isTrue(plugin.getGlobal())) {
                pluginDriverClassNames.put(plugin.getId(), pluginLoadService.getImplClass(plugin.getId(), Driver.class).getName());
            } else {
                //判断组织id
                if (StringUtils.isNotBlank(organizationId)) {
                    //判断组织id是否在插件组织id中
                    PluginOrganizationExample pluginOrganizationExample = new PluginOrganizationExample();
                    pluginOrganizationExample.createCriteria().andPluginIdEqualTo(plugin.getId()).andOrganizationIdEqualTo(organizationId);
                    List<PluginOrganization> pluginOrganizations = pluginOrganizationMapper.selectByExample(pluginOrganizationExample);
                    if (pluginOrganizations.size() > 0) {
                        pluginDriverClassNames.put(plugin.getId(), pluginLoadService.getImplClass(plugin.getId(), Driver.class).getName());
                    }
                }
            }
        });
        // 已经内置了 mysql 依赖
        pluginDriverClassNames.put(StringUtils.EMPTY, "com.mysql.jdbc.Driver");
        return pluginDriverClassNames;
    }

    public void delete(String id) {
        environmentMapper.deleteByPrimaryKey(id);
        environmentBlobMapper.deleteByPrimaryKey(id);
    }

    public EnvironmentRequest add(EnvironmentRequest environmentRequest, String userId, List<MultipartFile> sslFiles) {
        Environment environment = new Environment();
        environment.setId(UUID.randomUUID().toString());
        environment.setCreateUser(userId);
        environment.setName(environmentRequest.getName());
        checkEnvironmentExist(environment);
        environment.setCreateTime(System.currentTimeMillis());
        environment.setProjectId(environmentRequest.getProjectId());
        environmentMapper.insert(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        String config = JSON.toJSONString(environmentRequest.getConfig());
        environmentBlob.setConfig(config.getBytes());
        environmentBlobMapper.insert(environmentBlob);
        if (CollectionUtils.isNotEmpty(sslFiles)) {
            sslFiles.forEach(sslFile -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(sslFile.getOriginalFilename());
                fileRequest.setProjectId(environment.getProjectId());
                fileRequest.setResourceId(environment.getId());
                try {
                    minioRepository.saveFile(sslFile, fileRequest);
                } catch (Exception e) {
                    LogUtils.info("上传ssl文件失败:  文件名称:" + sslFile.getOriginalFilename(), e);
                }
            });
        }
        return environmentRequest;
    }

    public List<Environment> list(String projectId) {
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        example.setOrderByClause("update_time desc");
        return environmentMapper.selectByExample(example);
    }

    public EnvironmentRequest get(String environmentId) {
        EnvironmentRequest environmentRequest = new EnvironmentRequest();
        Environment environment = environmentMapper.selectByPrimaryKey(environmentId);
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(environmentId);
        environmentRequest.setProjectId(environment.getProjectId());
        environmentRequest.setName(environment.getName());
        environmentRequest.setId(environment.getId());
        if (environmentBlob == null) {
            return environmentRequest;
        }
        environmentRequest.setConfig(JSON.parseObject(Arrays.toString(environmentBlob.getConfig()), EnvironmentConfig.class));
        return environmentRequest;
    }

    public List<EnvironmentRequest> export(List<String> environmentIds) {
        if (CollectionUtils.isNotEmpty(environmentIds)) {
            // 查询环境
            EnvironmentExample environmentExample = new EnvironmentExample();
            environmentExample.createCriteria().andIdIn(environmentIds);
            List<Environment> environments = environmentMapper.selectByExample(environmentExample);
            Map<String, Environment> environmentMap = new HashMap<>();
            environments.forEach(environment -> environmentMap.put(environment.getId(), environment));
            // 查询环境配置
            EnvironmentBlobExample environmentBlobExample = new EnvironmentBlobExample();
            environmentBlobExample.createCriteria().andIdIn(environmentIds);
            List<EnvironmentBlob> environmentBlobs = environmentBlobMapper.selectByExample(environmentBlobExample);
            Map<String, EnvironmentBlob> environmentBlobMap = new HashMap<>();
            environmentBlobs.forEach(environmentBlob -> environmentBlobMap.put(environmentBlob.getId(), environmentBlob));

            List<EnvironmentRequest> environmentRequests = new ArrayList<>();
            environmentIds.forEach(environmentId -> {
                EnvironmentRequest environmentRequest = new EnvironmentRequest();
                Environment environment = environmentMap.get(environmentId);
                EnvironmentBlob environmentBlob = environmentBlobMap.get(environmentId);
                environmentRequest.setProjectId(environment.getProjectId());
                environmentRequest.setName(environment.getName());
                environmentRequest.setId(environment.getId());
                if (environmentBlob != null) {
                    environmentRequest.setConfig(JSON.parseObject(Arrays.toString(environmentBlob.getConfig()), EnvironmentConfig.class));
                }
                environmentRequests.add(environmentRequest);
            });
            return environmentRequests;
        } else {
            return new ArrayList<>();
        }
    }

    public void checkEnvironmentExist(Environment environment) {
        if (environment.getName() != null) {
            EnvironmentExample environmentExample = new EnvironmentExample();
            environmentExample.createCriteria().andNameEqualTo(environment.getName()).andProjectIdEqualTo(environment.getProjectId()).andIdNotEqualTo(environment.getId());
            if (environmentMapper.selectByExample(environmentExample).size() > 0) {
                throw new MSException(Translator.get("api_test_environment_already_exists"));
            }
        }
    }

    public void create(MultipartFile file, String userId, String currentProjectId) {
        if (file != null) {
            try {
                InputStream inputStream = file.getInputStream();
                // 读取文件内容
                String content = new String(inputStream.readAllBytes());
                inputStream.close();
                // 拿到的参数是一个list
                List<EnvironmentRequest> environmentRequests = JSON.parseArray(content, EnvironmentRequest.class);
                if (CollectionUtils.isNotEmpty(environmentRequests)) {
                    environmentRequests.forEach(environmentRequest -> {
                        Environment environment = new Environment();
                        environment.setId(UUID.randomUUID().toString());
                        environment.setCreateUser(userId);
                        environment.setName(environmentRequest.getName());
                        checkEnvironmentExist(environment);
                        environment.setCreateTime(System.currentTimeMillis());
                        environment.setProjectId(currentProjectId);
                        environmentMapper.insert(environment);
                        EnvironmentBlob environmentBlob = new EnvironmentBlob();
                        environmentBlob.setId(environment.getId());
                        String config = JSON.toJSONString(environmentRequest.getConfig());
                        environmentBlob.setConfig(config.getBytes());
                        environmentBlobMapper.insert(environmentBlob);
                    });
                }
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }
}
