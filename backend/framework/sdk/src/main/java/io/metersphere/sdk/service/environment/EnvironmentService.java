package io.metersphere.sdk.service.environment;

import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentBlobExample;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.environment.EnvironmentConfig;
import io.metersphere.sdk.dto.environment.EnvironmentConfigRequest;
import io.metersphere.sdk.dto.environment.dataSource.DataSource;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.service.JdbcDriverPluginService;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;


@Service
@Transactional
public class EnvironmentService {
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private MinioRepository minioRepository;
    @Resource
    private JdbcDriverPluginService jdbcDriverPluginService;

    public List<OptionDTO> getDriverOptions(String organizationId) {
        return jdbcDriverPluginService.getJdbcDriverOption(organizationId);
    }

    public void validateDataSource(DataSource databaseConfig) {
        try {
            if (StringUtils.isNotBlank(databaseConfig.getDriverId())) {
                Driver driver = jdbcDriverPluginService.getDriverByOptionId(databaseConfig.getDriverId());
                Properties properties = new Properties();
                properties.setProperty("user", databaseConfig.getUsername());
                properties.setProperty("password", databaseConfig.getPassword());
                driver.connect(databaseConfig.getDbUrl(), properties);
            } else {
                DriverManager.getConnection(databaseConfig.getDbUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String id) {
        environmentMapper.deleteByPrimaryKey(id);
        environmentBlobMapper.deleteByPrimaryKey(id);
    }

    public EnvironmentConfigRequest add(EnvironmentConfigRequest environmentConfigRequest, String userId, List<MultipartFile> sslFiles) {
        Environment environment = new Environment();
        environment.setId(UUID.randomUUID().toString());
        environment.setCreateUser(userId);
        environment.setName(environmentConfigRequest.getName());
        checkEnvironmentExist(environment);
        environment.setCreateTime(System.currentTimeMillis());
        environment.setProjectId(environmentConfigRequest.getProjectId());
        environmentMapper.insert(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        String config = JSON.toJSONString(environmentConfigRequest.getConfig());
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
        return environmentConfigRequest;
    }

    public List<Environment> list(String projectId) {
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        example.setOrderByClause("update_time desc");
        return environmentMapper.selectByExample(example);
    }

    public EnvironmentConfigRequest get(String environmentId) {
        EnvironmentConfigRequest environmentConfigRequest = new EnvironmentConfigRequest();
        Environment environment = environmentMapper.selectByPrimaryKey(environmentId);
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(environmentId);
        environmentConfigRequest.setProjectId(environment.getProjectId());
        environmentConfigRequest.setName(environment.getName());
        environmentConfigRequest.setId(environment.getId());
        if (environmentBlob == null) {
            return environmentConfigRequest;
        }
        environmentConfigRequest.setConfig(JSON.parseObject(Arrays.toString(environmentBlob.getConfig()), EnvironmentConfig.class));
        return environmentConfigRequest;
    }

    public List<EnvironmentConfigRequest> export(List<String> environmentIds) {
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

            List<EnvironmentConfigRequest> environmentConfigRequests = new ArrayList<>();
            environmentIds.forEach(environmentId -> {
                EnvironmentConfigRequest environmentConfigRequest = new EnvironmentConfigRequest();
                Environment environment = environmentMap.get(environmentId);
                EnvironmentBlob environmentBlob = environmentBlobMap.get(environmentId);
                environmentConfigRequest.setProjectId(environment.getProjectId());
                environmentConfigRequest.setName(environment.getName());
                environmentConfigRequest.setId(environment.getId());
                if (environmentBlob != null) {
                    environmentConfigRequest.setConfig(JSON.parseObject(Arrays.toString(environmentBlob.getConfig()), EnvironmentConfig.class));
                }
                environmentConfigRequests.add(environmentConfigRequest);
            });
            return environmentConfigRequests;
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
                List<EnvironmentConfigRequest> environmentConfigRequests = JSON.parseArray(content, EnvironmentConfigRequest.class);
                if (CollectionUtils.isNotEmpty(environmentConfigRequests)) {
                    environmentConfigRequests.forEach(environmentConfigRequest -> {
                        Environment environment = new Environment();
                        environment.setId(UUID.randomUUID().toString());
                        environment.setCreateUser(userId);
                        environment.setName(environmentConfigRequest.getName());
                        checkEnvironmentExist(environment);
                        environment.setCreateTime(System.currentTimeMillis());
                        environment.setProjectId(currentProjectId);
                        environmentMapper.insert(environment);
                        EnvironmentBlob environmentBlob = new EnvironmentBlob();
                        environmentBlob.setId(environment.getId());
                        String config = JSON.toJSONString(environmentConfigRequest.getConfig());
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
