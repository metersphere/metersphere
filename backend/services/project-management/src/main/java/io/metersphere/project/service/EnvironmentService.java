package io.metersphere.project.service;


import io.metersphere.plugin.api.spi.AbstractProtocolPlugin;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.environment.*;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.domain.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentGroupRelationMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.PluginScript;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.service.JdbcDriverPluginService;
import io.metersphere.system.service.PluginScriptService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentService extends MoveNodeService {
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
    @Resource
    private ProjectService projectService;
    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private PluginScriptService pluginScriptService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private EnvironmentGroupRelationMapper environmentGroupRelationMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String PATH = "/project/environment/import";
    private static final String MOCK_EVN_SOCKET = "/mock-server/";
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    public List<OptionDTO> getDriverOptions(String organizationId) {
        return jdbcDriverPluginService.getJdbcDriverOption(organizationId);
    }

    public void validateDataSource(DataSource databaseConfig) {
        try {
            Driver driver = jdbcDriverPluginService.getDriverByOptionId(databaseConfig.getDriverId());
            Properties properties = new Properties();
            properties.setProperty(USERNAME, databaseConfig.getUsername());
            properties.setProperty(PASSWORD, databaseConfig.getPassword());
            Connection connect = driver.connect(databaseConfig.getDbUrl(), properties);
            if (connect == null) {
                throw new MSException(Translator.get("api_test_environment_datasource_connect_failed"));
            }
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
        fileRequest.setFolder(DefaultRepositoryDir.getEnvSslDir(environment.getProjectId(), environment.getId()));
        try {
            minioRepository.deleteFolder(fileRequest);
        } catch (Exception e) {
            LogUtils.info("删除ssl文件失败:  文件名称:" + environment.getName(), e);
        }
        environmentMapper.deleteByPrimaryKey(id);
        environmentBlobMapper.deleteByPrimaryKey(id);
        //删除环境组及联关系
        EnvironmentGroupRelationExample environmentGroupRelationExample = new EnvironmentGroupRelationExample();
        environmentGroupRelationExample.createCriteria().andEnvironmentIdEqualTo(id);
        environmentGroupRelationMapper.deleteByExample(environmentGroupRelationExample);

    }

    public List<Environment> list(EnvironmentFilterRequest request) {
        return extEnvironmentMapper.selectByKeyword(request.getKeyword(), false, request.getProjectId());
    }

    public Environment add(EnvironmentRequest request, String userId, List<MultipartFile> sslFiles) {
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
        environment.setDescription(request.getDescription());
        environment.setPos(getNextOrder(request.getProjectId()));
        environmentMapper.insert(environment);
        request.setId(environment.getId());
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        if (request.getConfig() == null) {
            request.setConfig(new EnvironmentConfig());
        }
        environmentBlob.setConfig(JSON.toJSONBytes(request.getConfig()));
        environmentBlobMapper.insert(environmentBlob);
        uploadFileToMinio(sslFiles, environment);
        return environment;
    }

    private void uploadFileToMinio(List<MultipartFile> sslFiles, Environment environment) {
        if (CollectionUtils.isNotEmpty(sslFiles)) {
            sslFiles.forEach(sslFile -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(sslFile.getOriginalFilename());
                fileRequest.setFolder(DefaultRepositoryDir.getEnvSslDir(environment.getProjectId(), environment.getId()));
                try {
                    minioRepository.saveFile(sslFile, fileRequest);
                } catch (Exception e) {
                    LogUtils.info("上传ssl文件失败:  文件名称:" + sslFile.getOriginalFilename(), e);
                    throw new MSException(Translator.get("api_test_environment_ssl_file_upload_failed"));
                }
            });
        }
    }

    public EnvironmentInfoDTO get(String environmentId) {
        if (StringUtils.isBlank(environmentId)) {
            return null;
        }
        EnvironmentInfoDTO environmentInfoDTO = new EnvironmentInfoDTO();
        Environment environment = environmentMapper.selectByPrimaryKey(environmentId);
        if (environment == null) {
            return null;
        }
        environmentInfoDTO.setProjectId(environment.getProjectId());
        environmentInfoDTO.setName(environment.getName());
        environmentInfoDTO.setId(environment.getId());
        environmentInfoDTO.setMock(environment.getMock());
        BeanUtils.copyBean(environmentInfoDTO, environment);
        environmentInfoDTO.setConfig(getEnvironmentConfig(environmentId));

        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String baseUrl = baseSystemConfigDTO.getUrl();

        handleMockEnv(baseUrl, environment, environmentInfoDTO);

        return environmentInfoDTO;
    }

    public EnvironmentConfig getEnvironmentConfig(String environmentId) {
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(environmentId);
        if (environmentBlob == null) {
            return new EnvironmentConfig();
        } else {
            return JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class);
        }
    }

    public long getNextOrder(String projectId) {
        Long pos = extEnvironmentMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    public ResponseEntity<byte[]> exportJson(TableBatchProcessDTO request, String projectId) {
        try {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            List<EnvironmentRequest> environmentList = this.exportEnv(request, projectId);
            String fileName = null;
            if (CollectionUtils.isNotEmpty(environmentList)) {
                if (environmentList.size() == 1) {
                    fileName = StringUtils.join(project.getName(), "_", environmentList.getFirst().getName(), ".json");
                } else {
                    fileName = StringUtils.join(project.getName(), "_", Translator.get("env_info_all"));
                }
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(JSON.toJSONString(environmentList).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    public List<EnvironmentRequest> exportEnv(TableBatchProcessDTO request, String projectId) {
        List<String> environmentIds = this.getEnvironmentIds(request, projectId);
        // 查询环境
        List<Environment> environments = getEnvironmentsByIds(environmentIds);
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
        return environmentRequests;
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

    private List<String> getEnvironmentIds(TableBatchProcessDTO request, String projectId) {
        if (request.isSelectAll()) {
            List<Environment> environments = extEnvironmentMapper.selectByKeyword(request.getCondition().getKeyword(), true, projectId);
            List<String> environmentIds = environments.stream().map(Environment::getId).collect(Collectors.toList());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                environmentIds.removeAll(request.getExcludeIds());
            }
            return environmentIds;
        } else {
            return request.getSelectIds();
        }
    }

    public Environment getExitsEnv(Environment environment) {
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andNameEqualTo(environment.getName()).andProjectIdEqualTo(environment.getProjectId()).andIdNotEqualTo(environment.getId());
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        if (CollectionUtils.isNotEmpty(environments)) {
            return environments.getFirst();
        }
        return null;
    }

    public Environment update(EnvironmentRequest request, String userId, List<MultipartFile> sslFiles) {
        Environment environment = new Environment();
        environment.setId(request.getId());
        environment.setUpdateUser(userId);
        environment.setProjectId(request.getProjectId());
        environment.setName(request.getName());
        environment.setDescription(request.getDescription());
        checkEnvironmentExist(environment);
        environment.setUpdateTime(System.currentTimeMillis());
        environmentMapper.updateByPrimaryKeySelective(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        if (request.getConfig() != null) {
            environmentBlob.setConfig(JSON.toJSONBytes(request.getConfig()));
            environmentBlobMapper.updateByPrimaryKeySelective(environmentBlob);
        }
        uploadFileToMinio(sslFiles, environment);
        return environment;
    }

    public void create(EnvironmentImportRequest request, MultipartFile file, String userId, String currentProjectId) {
        if (file != null) {
            try {
                InputStream inputStream = file.getInputStream();
                // 读取文件内容
                String content = new String(inputStream.readAllBytes());
                inputStream.close();
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
                        environment.setPos(getNextOrder(currentProjectId));
                        Environment exitsEnv = getExitsEnv(environment);
                        if (exitsEnv != null && BooleanUtils.isTrue(request.getCover())) {
                            environmentMapper.deleteByPrimaryKey(exitsEnv.getId());
                            environmentBlobMapper.deleteByPrimaryKey(exitsEnv.getId());
                        } else if (exitsEnv != null && BooleanUtils.isFalse(request.getCover())) {
                            return;
                        }
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
                    if (CollectionUtils.isNotEmpty(environments)
                            && CollectionUtils.isNotEmpty(environmentBlobs)
                            && CollectionUtils.isNotEmpty(logDos)) {
                        environmentMapper.batchInsert(environments);
                        environmentBlobMapper.batchInsert(environmentBlobs);
                        operationLogService.batchAdd(logDos);
                    }
                }
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }

    public List<EnvironmentPluginScriptDTO> getPluginScripts(String projectId) {
        Project project = projectService.checkProjectNotExist(projectId);
        // 查询组织下有权限的接口插件
        List<PluginWrapper> orgApiPluginWrappers = apiPluginService.getOrgApiPluginWrappers(project.getOrganizationId());

        // 接口协议环境脚本
        List<PluginScript> pluginScripts = new ArrayList<>(orgApiPluginWrappers.size());
        orgApiPluginWrappers.forEach(wrapper -> {
            Plugin plugin = wrapper.getPlugin();
            if (plugin instanceof AbstractProtocolPlugin protocolPlugin) {
                Optional.ofNullable(pluginScriptService.get(wrapper.getPluginId(), protocolPlugin.getEnvProtocolScriptId()))
                        .ifPresent(pluginScripts::add);
            }
        });

        // 返回环境脚本列表
        return pluginScripts.stream().map(pluginScript -> {
            EnvironmentPluginScriptDTO envPluginScript = new EnvironmentPluginScriptDTO();
            envPluginScript.setPluginId(pluginScript.getPluginId());
            envPluginScript.setScript(JSON.parseObject(new String(pluginScript.getScript())));
            return envPluginScript;
        }).toList();
    }

    public List<EnvironmentInfoDTO> getByIds(List<String> envIds) {
        if (CollectionUtils.isEmpty(envIds)) {
            return Collections.emptyList();
        }

        List<Environment> environments = getEnvironmentsByIds(envIds);
        Map<String, EnvironmentBlob> envBlobMap = getEnvironmentBlobsByIds(envIds).stream().
                collect(Collectors.toMap(EnvironmentBlob::getId, Function.identity()));

        List<String> projectIds = environments.stream()
                .map(Environment::getProjectId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Project> projectMap = getProjects(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        String baseUrl = systemParameterService.getBaseInfo().getUrl();

        List<EnvironmentInfoDTO> environmentInfos = new ArrayList<>();
        for (Environment environment : environments) {
            EnvironmentInfoDTO environmentInfo = BeanUtils.copyBean(new EnvironmentInfoDTO(), environment);
            EnvironmentBlob environmentBlob = envBlobMap.get(environment.getId());
            if (environmentBlob.getConfig() != null) {
                environmentInfo.setConfig(JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class));
            }

            handleMockEnv(baseUrl, environment, environmentInfo, projectMap);

            environmentInfos.add(environmentInfo);
        }
        return environmentInfos;
    }

    private void handleMockEnv(String baseUrl, Environment environment, EnvironmentInfoDTO environmentInfo) {
        if (BooleanUtils.isTrue(environment.getMock())) {
            Project project = projectMapper.selectByPrimaryKey(environment.getProjectId());
            handleMockEnv(baseUrl, environment, environmentInfo, Map.of(project.getId(), project));
        }
    }

    private void handleMockEnv(String baseUrl, Environment environment, EnvironmentInfoDTO environmentInfo, Map<String, Project> projectMap) {
        if (BooleanUtils.isTrue(environment.getMock())) {
            if (StringUtils.isNotEmpty(baseUrl)) {
                Project project = projectMap.get(environment.getProjectId());
                String domain = baseUrl.replace(HTTP, StringUtils.EMPTY).replace(HTTPS, StringUtils.EMPTY);
                String protocol = baseUrl.substring(0, baseUrl.indexOf(domain) - 3);
                if (environmentInfo.getConfig() != null && CollectionUtils.isNotEmpty(environmentInfo.getConfig().getHttpConfig())) {
                    environmentInfo.getConfig().getHttpConfig().getFirst().setId(IDGenerator.nextStr());
                    environmentInfo.getConfig().getHttpConfig().getFirst().setProtocol(protocol);
                    environmentInfo.getConfig().getHttpConfig().getFirst().setHostname(StringUtils.join(domain, MOCK_EVN_SOCKET, project.getNum()));
                    environmentInfo.getConfig().getHttpConfig().getFirst().setUrl(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
                } else {
                    List<HttpConfig> httpConfigs = new ArrayList<>();
                    HttpConfig httpConfig = new HttpConfig();
                    httpConfig.setId(IDGenerator.nextStr());
                    httpConfig.setProtocol(protocol);
                    httpConfig.setHostname(StringUtils.join(domain, MOCK_EVN_SOCKET, project.getNum()));
                    httpConfig.setUrl(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
                    httpConfigs.add(httpConfig);
                    environmentInfo.getConfig().setHttpConfig(httpConfigs);
                }
            }
        }
    }

    private List<Project> getProjects(List<String> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        ProjectExample example = new ProjectExample();
        example.createCriteria().andIdIn(projectIds);
        return projectMapper.selectByExample(example);
    }

    public List<Environment> getEnvironmentsByIds(List<String> envIds) {
        if (CollectionUtils.isEmpty(envIds)) {
            return Collections.emptyList();
        }
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andIdIn(envIds);
        return environmentMapper.selectByExample(example);
    }

    public List<EnvironmentBlob> getEnvironmentBlobsByIds(List<String> envIds) {
        if (CollectionUtils.isEmpty(envIds)) {
            return Collections.emptyList();
        }
        EnvironmentBlobExample example = new EnvironmentBlobExample();
        example.createCriteria().andIdIn(envIds);
        return environmentBlobMapper.selectByExampleWithBLOBs(example);
    }

    public List<EnvironmentOptionsDTO> listOption(String projectId) {
        List<EnvironmentOptionsDTO> environmentOptions = new ArrayList<>();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(projectId);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        List<String> ids = environments.stream().map(Environment::getId).toList();
        EnvironmentBlobExample environmentBlobExample = new EnvironmentBlobExample();
        environmentBlobExample.createCriteria().andIdIn(ids);
        List<EnvironmentBlob> environmentBlobs = environmentBlobMapper.selectByExampleWithBLOBs(environmentBlobExample);
        Map<String, EnvironmentBlob> environmentBlobMap = environmentBlobs.stream().collect(Collectors.toMap(EnvironmentBlob::getId, Function.identity()));
        environments.forEach(environment -> {
            EnvironmentOptionsDTO environmentOptionsDTO = new EnvironmentOptionsDTO();
            BeanUtils.copyBean(environmentOptionsDTO, environment);
            EnvironmentBlob environmentBlob = environmentBlobMap.get(environment.getId());
            if (environmentBlob != null) {
                EnvironmentConfig environmentConfig = JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class);
                if (environmentConfig != null && CollectionUtils.isNotEmpty(environmentConfig.getHttpConfig())) {
                    environmentOptionsDTO.setDomain(environmentConfig.getHttpConfig());
                }
                if (BooleanUtils.isTrue(environment.getMock())) {
                    SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
                    if (systemParameterService != null) {
                        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
                        String baseUrl = baseSystemConfigDTO.getUrl();
                        if (StringUtils.isNotEmpty(baseUrl)) {
                            Project project = projectMapper.selectByPrimaryKey(environment.getProjectId());
                            environmentOptionsDTO.getDomain().getFirst().setHostname(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
                        }
                    }
                }
            }
            environmentOptions.add(environmentOptionsDTO);
        });
        return environmentOptions;
    }

    @Override
    public void updatePos(String id, long pos) {
        extEnvironmentMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> caseIdList = extEnvironmentMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtEnvironmentMapper batchUpdateMapper = sqlSession.getMapper(ExtEnvironmentMapper.class);
        for (int i = 0; i < caseIdList.size(); i++) {
            batchUpdateMapper.updatePos(caseIdList.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public void moveNode(PosRequest posRequest) {
        NodeMoveRequest request = super.getNodeMoveRequest(posRequest,true);
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                posRequest.getProjectId(),
                request,
                extEnvironmentMapper::selectDragInfoById,
                extEnvironmentMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
    }
}
