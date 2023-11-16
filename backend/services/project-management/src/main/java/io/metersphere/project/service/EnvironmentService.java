package io.metersphere.project.service;


import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentDTO;
import io.metersphere.project.dto.environment.EnvironmentExportDTO;
import io.metersphere.project.dto.environment.EnvironmentRequest;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentBlobExample;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.service.JdbcDriverPluginService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Driver;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ExtEnvironmentMapper extEnvironmentMapper;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String PATH = "/project/environment/import";
    private static final String MOCK_EVN_SOCKET = "/api/mock/";

    private static final String MAIN_FOLDER_PROJECT = "project";
    private static final String APP_NAME_ENVIRONMENT = "environment";

    public List<OptionDTO> getDriverOptions(String organizationId) {
        return jdbcDriverPluginService.getJdbcDriverOption(organizationId);
    }

    public void validateDataSource(DataSource databaseConfig) {
        try {
            Driver driver = jdbcDriverPluginService.getDriverByOptionId(databaseConfig.getDriverId());
            Properties properties = new Properties();
            properties.setProperty(USERNAME, databaseConfig.getUsername());
            properties.setProperty(PASSWORD, databaseConfig.getPassword());
            driver.connect(databaseConfig.getDbUrl(), properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String id) {
        Environment environment = environmentMapper.selectByPrimaryKey(id);
        if (environment == null) {
            throw new MSException(Translator.get("api_test_environment_not_exist"));
        }
        if (BooleanUtils.isTrue(environment.getMock())) {
            throw new MSException(Translator.get("mock_environment_not_delete"));
        }
        //删除环境的ssl文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(minioEnvPath(environment.getProjectId()));
        fileRequest.setResourceId(id);
        try {
            minioRepository.deleteFolder(fileRequest);
        } catch (Exception e) {
            LogUtils.info("删除ssl文件失败:  文件名称:" + environment.getName(), e);
        }
        environmentMapper.deleteByPrimaryKey(id);
        environmentBlobMapper.deleteByPrimaryKey(id);

    }

    public List<Environment> list(EnvironmentDTO request) {
        return extEnvironmentMapper.selectByKeyword(request.getKeyword(), false, request.getProjectId());
    }

    public EnvironmentRequest add(EnvironmentRequest request, String userId, List<MultipartFile> sslFiles) {
        Environment environment = new Environment();
        environment.setId(IDGenerator.nextStr());
        environment.setCreateUser(userId);
        environment.setName(request.getName());
        environment.setProjectId(request.getProjectId());
        checkEnvironmentExist(environment);
        environment.setCreateTime(System.currentTimeMillis());
        environment.setUpdateUser(userId);
        environment.setUpdateTime(System.currentTimeMillis());
        environment.setMock(false);
        environmentMapper.insert(environment);
        request.setId(environment.getId());
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        environmentBlob.setConfig(JSON.toJSONBytes(request.getConfig()));
        environmentBlobMapper.insert(environmentBlob);
        uploadFileToMinio(sslFiles, environment);
        return request;
    }

    private void uploadFileToMinio(List<MultipartFile> sslFiles, Environment environment) {
        if (CollectionUtils.isNotEmpty(sslFiles)) {
            sslFiles.forEach(sslFile -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFolder(minioEnvPath(environment.getProjectId()));
                fileRequest.setFileName(sslFile.getName());
                fileRequest.setResourceId(environment.getId());
                try {
                    minioRepository.saveFile(sslFile, fileRequest);
                } catch (Exception e) {
                    LogUtils.info("上传ssl文件失败:  文件名称:" + sslFile.getName(), e);
                    throw new MSException(Translator.get("api_test_environment_ssl_file_upload_failed"));
                }
            });
        }
    }

    public EnvironmentRequest get(String environmentId) {
        EnvironmentRequest environmentRequest = new EnvironmentRequest();
        Environment environment = environmentMapper.selectByPrimaryKey(environmentId);
        if (environment == null) {
            return null;
        }
        environmentRequest.setProjectId(environment.getProjectId());
        environmentRequest.setName(environment.getName());
        environmentRequest.setId(environment.getId());
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(environmentId);
        if (environmentBlob == null) {
            environmentRequest.setConfig(new EnvironmentConfig());
        } else {
            environmentRequest.setConfig(JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class));
        }
        if (BooleanUtils.isTrue(environment.getMock())) {
            SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
            assert systemParameterService != null;
            BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
            String baseUrl = baseSystemConfigDTO.getUrl();
            if (StringUtils.isNotEmpty(baseUrl)) {
                Project project = projectMapper.selectByPrimaryKey(environment.getProjectId());
                environmentRequest.getConfig().getHttpConfig().get(0).setUrl(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
            }
        }

        return environmentRequest;
    }

    public String export(EnvironmentExportDTO environmentExportDTO) {
        List<String> environmentIds = this.getEnvironmentIds(environmentExportDTO);
        // 查询环境
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(environmentIds);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        Map<String, Environment> environmentMap = new HashMap<>();
        environments.forEach(environment -> environmentMap.put(environment.getId(), environment));
        // 查询环境配置
        EnvironmentBlobExample environmentBlobExample = new EnvironmentBlobExample();
        environmentBlobExample.createCriteria().andIdIn(environmentIds);
        List<EnvironmentBlob> environmentBlobs = environmentBlobMapper.selectByExampleWithBLOBs(environmentBlobExample);
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
                environmentRequest.setConfig(JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class));
            }
            environmentRequests.add(environmentRequest);
        });
        return JSON.toJSONString(environmentRequests);
    }

    private List<String> getEnvironmentIds(EnvironmentExportDTO request) {
        if (request.isSelectAll()) {
            List<Environment> environments = extEnvironmentMapper.selectByKeyword(request.getCondition().getKeyword(), true, request.getProjectId());
            List<String> environmentIds = environments.stream().map(Environment::getId).collect(Collectors.toList());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                environmentIds.removeAll(request.getExcludeIds());
            }
            return environmentIds;
        } else {
            return request.getSelectIds();
        }
    }

    public void checkEnvironmentExist(Environment environment) {
        if (environment.getName() != null) {
            EnvironmentExample environmentExample = new EnvironmentExample();
            environmentExample.createCriteria().andNameEqualTo(environment.getName()).andProjectIdEqualTo(environment.getProjectId()).andIdNotEqualTo(environment.getId());
            if (!environmentMapper.selectByExample(environmentExample).isEmpty()) {
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
                    List<Environment> environments = new ArrayList<>();
                    List<EnvironmentBlob> environmentBlobs = new ArrayList<>();
                    List<LogDTO> logDos = new ArrayList<>();
                    Project project = projectMapper.selectByPrimaryKey(currentProjectId);
                    environmentRequests.forEach(environmentRequest -> {
                        Environment environment = new Environment();
                        environment.setId(IDGenerator.nextStr());
                        environment.setCreateUser(userId);
                        environment.setName(environmentRequest.getName());
                        environment.setProjectId(currentProjectId);
                        environment.setUpdateUser(userId);
                        environment.setUpdateTime(System.currentTimeMillis());
                        environment.setMock(false);
                        checkEnvironmentExist(environment);
                        environment.setCreateTime(System.currentTimeMillis());
                        environment.setProjectId(currentProjectId);
                        environments.add(environment);
                        EnvironmentBlob environmentBlob = new EnvironmentBlob();
                        environmentBlob.setId(environment.getId());
                        environmentBlob.setConfig(JSON.toJSONBytes(environmentRequest.getConfig()));
                        environmentBlobs.add(environmentBlob);
                        LogDTO logDTO = new LogDTO(
                                currentProjectId,
                                project.getOrganizationId(),
                                environment.getId(),
                                userId,
                                OperationLogType.ADD.name(),
                                OperationLogModule.PROJECT_ENVIRONMENT_SETTING,
                                environment.getName());
                        logDTO.setMethod(HttpMethodConstants.POST.name());
                        logDTO.setOriginalValue(JSON.toJSONBytes(environmentRequest.getConfig()));
                        logDTO.setPath(PATH);
                        logDos.add(logDTO);
                    });
                    environmentMapper.batchInsert(environments);
                    environmentBlobMapper.batchInsert(environmentBlobs);
                    operationLogService.batchAdd(logDos);
                }
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }

    public EnvironmentRequest update(EnvironmentRequest request, String userId, List<MultipartFile> sslFiles) {
        Environment environment = new Environment();
        environment.setId(request.getId());
        environment.setUpdateUser(userId);
        environment.setProjectId(request.getProjectId());
        environment.setName(request.getName());
        checkEnvironmentExist(environment);
        environment.setUpdateTime(System.currentTimeMillis());
        environmentMapper.updateByPrimaryKeySelective(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        environmentBlob.setConfig(JSON.toJSONBytes(request.getConfig()));
        environmentBlobMapper.updateByPrimaryKeySelective(environmentBlob);
        uploadFileToMinio(sslFiles, environment);
        return request;
    }

    private String minioEnvPath(String projectId) {
        return StringUtils.join(MAIN_FOLDER_PROJECT, "/", projectId, "/", APP_NAME_ENVIRONMENT);
    }

}
