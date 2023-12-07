package io.metersphere.project.service;


import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.*;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.domain.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.mapper.ProjectParametersMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.service.JdbcDriverPluginService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Driver;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
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
    public static final Long ORDER_STEP = 5000L;
    @Resource
    private ProjectParametersMapper projectParametersMapper;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String PATH = "/project/environment/import";
    private static final String MOCK_EVN_SOCKET = "/api/mock/";

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
        fileRequest.setFolder(DefaultRepositoryDir.getEnvSslDir(environment.getProjectId(), environment.getId()));
        try {
            minioRepository.deleteFolder(fileRequest);
        } catch (Exception e) {
            LogUtils.info("删除ssl文件失败:  文件名称:" + environment.getName(), e);
        }
        environmentMapper.deleteByPrimaryKey(id);
        environmentBlobMapper.deleteByPrimaryKey(id);

    }

    public List<Environment> list(EnvironmentFilterRequest request) {
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
        environment.setDescription(request.getDescription());
        environment.setPos(getNextOrder(request.getProjectId()));
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

    public Long getNextOrder(String projectId) {
        Long pos = extEnvironmentMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public ResponseEntity<byte[]> exportZip(EnvironmentExportRequest request) {
        try {
            byte[] bytes = this.download(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "files.zip")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    public byte[] download(EnvironmentExportRequest request) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        if (BooleanUtils.isTrue(request.getGlobalParam())) {
            //查询全局参数
            EnvironmentExportDTO environmentExportDTO = new EnvironmentExportDTO();
            ProjectParametersExample projectParametersExample = new ProjectParametersExample();
            projectParametersExample.createCriteria().andProjectIdEqualTo(request.getProjectId());
            List<ProjectParameters> projectParameters = projectParametersMapper.selectByExampleWithBLOBs(projectParametersExample);
            if (CollectionUtils.isNotEmpty(projectParameters)) {
                environmentExportDTO.setGlobalParam(true);
                environmentExportDTO.setData(new String(projectParameters.get(0).getParameters()));
                files.put(StringUtils.join(project.getName(), "_", Translator.get("global_params")), JSON.toJSONString(environmentExportDTO).getBytes());
            }
        }
        if (BooleanUtils.isTrue(request.getEnvironment())) {
            EnvironmentExportDTO environmentExportDTO = new EnvironmentExportDTO();
            List<EnvironmentRequest> environmentList = this.exportEnv(request);
            if (CollectionUtils.isNotEmpty(environmentList)) {
                environmentExportDTO.setEnvironment(true);
                environmentExportDTO.setData(JSON.toJSONString(environmentList));
                if (environmentList.size() == 1) {
                    files.put(StringUtils.join(project.getName(), "_", environmentList.get(0).getName(), ".json"), JSON.toJSONString(environmentList.get(0)).getBytes());
                } else {
                    files.put(StringUtils.join(project.getName(), "_", Translator.get("env_info_all")), JSON.toJSONString(environmentExportDTO).getBytes());
                }
            }
        }
        return FileDownloadUtils.listBytesToZip(files);
    }

    public List<EnvironmentRequest> exportEnv(EnvironmentExportRequest environmentExportRequest) {
        List<String> environmentIds = this.getEnvironmentIds(environmentExportRequest);
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

    private List<String> getEnvironmentIds(EnvironmentExportRequest request) {
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

    public Environment getExitsEnv(Environment environment) {
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andNameEqualTo(environment.getName()).andProjectIdEqualTo(environment.getProjectId()).andIdNotEqualTo(environment.getId());
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        if (CollectionUtils.isNotEmpty(environments)) {
            return environments.get(0);
        }
        return null;
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

    public void create(EnvironmentImportRequest request, MultipartFile file, String userId, String currentProjectId) {
        if (file != null) {
            try {
                InputStream inputStream = file.getInputStream();
                // 读取文件内容
                String content = new String(inputStream.readAllBytes());
                inputStream.close();
                //参数是一个对象
                EnvironmentExportDTO environmentExportDTO = JSON.parseObject(content, EnvironmentExportDTO.class);
                if (environmentExportDTO != null) {
                    if (BooleanUtils.isTrue(environmentExportDTO.getGlobalParam())) {
                        String data = environmentExportDTO.getData();
                        //解析出来的参数是一个对象
                        if (BooleanUtils.isTrue(request.getCover())) {
                            ProjectParametersExample projectParametersExample = new ProjectParametersExample();
                            projectParametersExample.createCriteria().andProjectIdEqualTo(currentProjectId);
                            if (projectParametersMapper.countByExample(projectParametersExample) > 0) {
                                projectParametersMapper.deleteByExample(projectParametersExample);
                            }
                            ProjectParameters projectParameters = new ProjectParameters();
                            projectParameters.setId(IDGenerator.nextStr());
                            projectParameters.setProjectId(currentProjectId);
                            projectParameters.setCreateUser(userId);
                            projectParameters.setUpdateUser(userId);
                            projectParameters.setCreateTime(System.currentTimeMillis());
                            projectParameters.setUpdateTime(System.currentTimeMillis());
                            projectParameters.setParameters(data.getBytes());
                            projectParametersMapper.insert(projectParameters);
                        }
                    }
                    if (BooleanUtils.isTrue(environmentExportDTO.getEnvironment())) {
                        // 拿到的参数是一个list
                        List<EnvironmentRequest> environmentRequests = JSON.parseArray(environmentExportDTO.getData(), EnvironmentRequest.class);
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
                    }
                }
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }

    public void editPos(PosRequest request) {
        ServiceUtils.updatePosField(request,
                Environment.class,
                environmentMapper::selectByPrimaryKey,
                extEnvironmentMapper::getPrePos,
                extEnvironmentMapper::getLastPos,
                environmentMapper::updateByPrimaryKeySelective);
    }
}
