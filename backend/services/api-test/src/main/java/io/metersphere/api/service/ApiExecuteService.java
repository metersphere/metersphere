package io.metersphere.api.service;

import io.metersphere.api.config.JmeterProperties;
import io.metersphere.api.config.KafkaConfig;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.debug.ApiDebugRunRequest;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.service.*;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.config.MinioProperties;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.service.*;
import io.metersphere.system.utils.TaskRunnerClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.api.controller.result.ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiExecuteService {
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RoundRobinService roundRobinService;
    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileManagementService fileManagementService;
    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private GlobalParamsService globalParamsService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private JdbcDriverPluginService jdbcDriverPluginService;

    @PostConstruct
    private void init() {
        String jmeterHome = getJmeterHome();
        JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter.properties");
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public String getJmeterHome() {
        String home = Objects.requireNonNull(getClass().getResource("/")).getPath() + "jmeter";
        try {
            File file = new File(home);
            if (file.exists()) {
                return home;
            } else {
                return jmeterProperties.getHome();
            }
        } catch (Exception e) {
            return jmeterProperties.getHome();
        }
    }

    public String getScriptRedisKey(String reportId, String testId) {
        return reportId + "_" + testId;
    }

    public TaskRequestDTO execute(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest, ApiParamConfig parameterConfig) {
        // 置minio kafka ms 等信息
        setServerInfoParam(taskRequest);

        // 设置执行文件参数
        setTaskFileParam(runRequest, taskRequest);

        //  误报处理 todo 多项目
        taskRequest.setMsRegexList(projectApplicationService.get(Collections.singletonList(taskRequest.getProjectId())));

        if (!StringUtils.equals(taskRequest.getResourceType(), ApiExecuteResourceType.API_DEBUG.name())) {
            // 设置全局参数，接口调试不使用全局参数
            parameterConfig.setGlobalParams(getGlobalParam(taskRequest.getProjectId()));
        }

        // 解析执行脚本
        String executeScript = parseExecuteScript(runRequest.getTestElement(), parameterConfig);

        // 设置插件文件信息 todo 多项目
        List<ApiExecuteFileInfo> pluginFiles = new ArrayList<>();
        pluginFiles.addAll(apiPluginService.getFileInfoByProjectId(taskRequest.getProjectId()));
        pluginFiles.addAll(jdbcDriverPluginService.getFileInfoByProjectId(taskRequest.getProjectId()));

        taskRequest.setPluginFiles(pluginFiles);

        // 将测试脚本缓存到 redis
        String scriptRedisKey = getScriptRedisKey(taskRequest.getReportId(), taskRequest.getResourceId());
        stringRedisTemplate.opsForValue().set(scriptRedisKey, executeScript);

        if (StringUtils.equals(taskRequest.getRunMode(), ApiExecuteRunMode.FRONTEND_DEBUG.name())) {
            // 清空mino和kafka配置信息，避免前端获取
            taskRequest.setMinioConfig(null);
            taskRequest.setKafkaConfig(null);
            // 前端调试返回执行参数，由前端调用本地资源池执行
            return taskRequest;
        }

        try {
            return doExecute(taskRequest);
        } catch (MSException e) {
            LogUtils.error(e);
            // 调用失败清理脚本
            stringRedisTemplate.delete(scriptRedisKey);
            throw e;
        } catch (Exception e) {
            LogUtils.error(e);
            // 调用失败清理脚本
            stringRedisTemplate.delete(scriptRedisKey);
            throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
        }
    }

    private GlobalParams getGlobalParam(String projectId) {
        GlobalParamsDTO globalParamsDTO = globalParamsService.get(projectId);
        if (globalParamsDTO != null) {
            return globalParamsDTO.getGlobalParams();
        }
        return null;
    }

    /**
     * 发送执行任务
     *
     * @param taskRequest 执行参数
     */
    private TaskRequestDTO doExecute(TaskRequestDTO taskRequest) throws Exception {
        // 获取资源池
        TestResourcePoolReturnDTO testResourcePoolDTO = getGetResourcePoolNodeDTO(taskRequest.getRunModeConfig(), taskRequest.getProjectId());
        TestResourceNodeDTO testResourceNodeDTO = getProjectExecuteNode(testResourcePoolDTO);
        if (StringUtils.isNotBlank(testResourcePoolDTO.getServerUrl())) {
            // 如果资源池配置了当前站点，则使用资源池的
            taskRequest.setMsUrl(testResourcePoolDTO.getServerUrl());
        }
        taskRequest.setPoolSize(testResourceNodeDTO.getConcurrentNumber());

        String endpoint = TaskRunnerClient.getEndpoint(testResourceNodeDTO.getIp(), testResourceNodeDTO.getPort());
        LogUtils.info("开始发送请求【 {}_{} 】到 {} 节点执行", taskRequest.getReportId(), taskRequest.getResourceId(), endpoint);
        if (StringUtils.equalsAny(taskRequest.getRunMode(), ApiExecuteRunMode.FRONTEND_DEBUG.name(), ApiExecuteRunMode.BACKEND_DEBUG.name())) {
            TaskRunnerClient.debugApi(endpoint, taskRequest);
        } else {
            TaskRunnerClient.runApi(endpoint, taskRequest);
        }

        // 清空mino和kafka配置信息，避免前端获取
        taskRequest.setMinioConfig(null);
        taskRequest.setKafkaConfig(null);

        return taskRequest;

    }

    private TestResourceNodeDTO getProjectExecuteNode(TestResourcePoolReturnDTO resourcePoolDTO) {
        roundRobinService.initializeNodes(resourcePoolDTO.getId(), resourcePoolDTO.getTestResourceReturnDTO().getNodesList());
        try {
            return roundRobinService.getNextNode(resourcePoolDTO.getId());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("get execute node error", e);
        }
    }

    private TestResourcePoolReturnDTO getGetResourcePoolNodeDTO(ApiRunModeConfigDTO runModeConfig, String projectId) {
        String poolId = runModeConfig.getPoolId();
        if (StringUtils.isBlank(poolId)) {
            poolId = getProjectApiResourcePoolId(projectId);
        }
        return getAvailableResourcePoolDTO(projectId, poolId);
    }

    /**
     * 设置minio kafka ms 等信息
     *
     * @param taskRequest 执行参数
     */
    private void setServerInfoParam(TaskRequestDTO taskRequest) {
        taskRequest.setKafkaConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(KafkaConfig.getKafkaConfig())));
        taskRequest.setMinioConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(getMinio())));
        taskRequest.setMsUrl(systemParameterService.getBaseInfo().getUrl());
    }

    /**
     * 公共脚本执行
     *
     * @param runRequest 执行参数
     * @return 报告ID
     */
    public TaskRequestDTO runScript(CustomFunctionRunRequest runRequest) {
        String reportId = runRequest.getReportId();
        String testId = runRequest.getProjectId();

        // 生成执行脚本
        MsScriptElement msScriptElement = new MsScriptElement();
        msScriptElement.setEnableCommonScript(true);
        msScriptElement.setName(runRequest.getReportId());
        msScriptElement.setProjectId(runRequest.getProjectId());
        CommonScriptInfo commonScriptInfo = new CommonScriptInfo();
        commonScriptInfo.setId(runRequest.getProjectId());
        commonScriptInfo.setParams(runRequest.getParams());
        commonScriptInfo.setScript(runRequest.getScript());
        commonScriptInfo.setScriptLanguage(runRequest.getType());
        commonScriptInfo.setName(runRequest.getReportId());
        msScriptElement.setCommonScriptInfo(commonScriptInfo);

        ApiResourceRunRequest apiRunRequest = new ApiResourceRunRequest();
        apiRunRequest.setTestElement(msScriptElement);

        // 设置执行参数
        TaskRequestDTO taskRequest = getTaskRequest(reportId, testId, runRequest.getProjectId());
        setServerInfoParam(taskRequest);
        taskRequest.setRealTime(true);
        taskRequest.setSaveResult(false);
        taskRequest.setResourceType(ApiExecuteResourceType.API_DEBUG.name());
        taskRequest.setRunMode(ApiExecuteRunMode.BACKEND_DEBUG.name());

        return execute(apiRunRequest, taskRequest, new ApiParamConfig());
    }

    /**
     * 给 taskRequest 设置文件相关参数
     *
     * @param runRequest  请求参数
     * @param taskRequest 执行参数
     */
    private void setTaskFileParam(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest) {
        setTaskRefFileParam(runRequest, taskRequest);
        setTaskTmpFileParam(runRequest, taskRequest);
        setTaskFuncJarParam(runRequest, taskRequest);
    }

    /**
     * 处理脚本执行所需要的jar包
     *
     * @param runRequest
     * @param taskRequest
     */
    private void setTaskFuncJarParam(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest) {
        Set<String> projectIdsSet = runRequest.getRefProjectIds();
        projectIdsSet.add(taskRequest.getProjectId());
        List<String> projectIds = projectIdsSet.stream().collect(Collectors.toList());

        // 获取函数jar包
        List<FileMetadata> fileMetadataList = fileManagementService.findJarByProjectId(projectIds);
        taskRequest.setFuncJars(getApiExecuteFileInfo(fileMetadataList));

        // TODO 当前项目没有包分两种情况，1 之前存在被删除，2 一直不存在
        //  为了兼容1 这种情况需要初始化一条空的数据，由执行机去做卸载
        if (CollectionUtils.isEmpty(taskRequest.getFuncJars())) {
            ApiExecuteFileInfo tempFileInfo = new ApiExecuteFileInfo();
            tempFileInfo.setProjectId(taskRequest.getProjectId());
            taskRequest.setFuncJars(List.of(tempFileInfo));
        }
    }

    /**
     * 处理没有保存的临时文件
     *
     * @param runRequest
     * @param taskRequest
     */
    private void setTaskTmpFileParam(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest) {
        // 没有保存的本地临时文件
        List<String> uploadFileIds = runRequest.getUploadFileIds();
        if (CollectionUtils.isNotEmpty(uploadFileIds)) {
            List<ApiExecuteFileInfo> localTempFiles = uploadFileIds.stream()
                    .map(tempFileId -> {
                        String fileName = apiFileResourceService.getTempFileNameByFileId(tempFileId);
                        return getApiExecuteFileInfo(tempFileId, fileName, taskRequest.getProjectId());
                    })
                    // uploadFileIds 查不到，则已经移动到正式目录
                    .filter(i -> StringUtils.isNotBlank(i.getFileName()))
                    .collect(Collectors.toList());
            taskRequest.setLocalTempFiles(localTempFiles);
        }

        List<String> linkFileIds = runRequest.getLinkFileIds();
        // 没有保存的文件管理临时文件
        if (CollectionUtils.isNotEmpty(linkFileIds)) {
            List<FileMetadata> fileMetadataList = fileMetadataService.getByFileIds(linkFileIds);
            // 添加临时的文件管理的文件
            taskRequest.getRefFiles().addAll(getApiExecuteFileInfo(fileMetadataList));
        }
    }

    /**
     * 处理运行的资源所关联的文件信息
     *
     * @param runRequest
     * @param taskRequest
     */
    private void setTaskRefFileParam(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest) {
        // 查询包括引用的资源所需的文件
        Set<String> resourceIdsSet = runRequest.getRefResourceIds();
        resourceIdsSet.add(taskRequest.getResourceId());
        List<String> resourceIds = resourceIdsSet.stream().collect(Collectors.toList());

        // 查询通过本地上传的文件
        List<ApiExecuteFileInfo> localFiles = apiFileResourceService.getByResourceIds(resourceIds).
                stream()
                .map(file -> {
                    ApiExecuteFileInfo apiExecuteFileInfo = getApiExecuteFileInfo(file.getFileId(), file.getFileName(), file.getProjectId());
                    // 本地上传的文件需要 resourceId 查询对应的目录
                    apiExecuteFileInfo.setResourceId(file.getResourceId());
                    apiExecuteFileInfo.setResourceType(file.getResourceType());
                    return apiExecuteFileInfo;
                })
                .collect(Collectors.toList());
        taskRequest.setLocalFiles(localFiles);

        // 查询关联的文件管理的文件
        List<ApiExecuteFileInfo> refFiles = fileAssociationService.getFiles(resourceIds).
                stream()
                .map(file -> {
                    ApiExecuteFileInfo refFileInfo = getApiExecuteFileInfo(file.getFileId(), file.getOriginalName(),
                            file.getProjectId(), file.getStorage());
                    if (StorageType.isGit(file.getStorage())) {
                        // 设置Git信息
                        refFileInfo.setFileMetadataRepositoryDTO(fileManagementService.getFileMetadataRepositoryDTO(file.getMetadataId()));
                        refFileInfo.setFileModuleRepositoryDTO(fileManagementService.getFileModuleRepositoryDTO(file.getModuleId()));
                    }
                    return refFileInfo;
                }).collect(Collectors.toList());
        taskRequest.setRefFiles(refFiles);
    }

    private List<ApiExecuteFileInfo> getApiExecuteFileInfo(List<FileMetadata> fileMetadataList) {
        return fileMetadataList.stream()
                .map(file -> {
                    ApiExecuteFileInfo tempFileInfo = getApiExecuteFileInfo(file.getId(), file.getOriginalName(),
                            file.getProjectId(), file.getStorage());
                    if (StorageType.isGit(file.getStorage())) {
                        // 设置Git信息
                        tempFileInfo.setFileMetadataRepositoryDTO(fileManagementService.getFileMetadataRepositoryDTO(file.getId()));
                        tempFileInfo.setFileModuleRepositoryDTO(fileManagementService.getFileModuleRepositoryDTO(file.getModuleId()));
                    }
                    return tempFileInfo;
                }).toList();
    }

    private ApiExecuteFileInfo getApiExecuteFileInfo(String fileId, String fileName, String projectId) {
        return getApiExecuteFileInfo(fileId, fileName, projectId, StorageType.MINIO.name());
    }

    private ApiExecuteFileInfo getApiExecuteFileInfo(String fileId, String fileName, String projectId, String storage) {
        ApiExecuteFileInfo apiExecuteFileInfo = new ApiExecuteFileInfo();
        apiExecuteFileInfo.setStorage(storage);
        apiExecuteFileInfo.setFileName(fileName);
        apiExecuteFileInfo.setFileId(fileId);
        apiExecuteFileInfo.setProjectId(projectId);
        return apiExecuteFileInfo;
    }

    /**
     * 生成执行脚本
     *
     * @param msTestElement 接口元素
     * @param config        参数配置
     * @return 执行脚本
     */
    private String parseExecuteScript(AbstractMsTestElement msTestElement, ParameterConfig config) {
        // 解析生成脚本
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        return defaultParser.parse(msTestElement, config);
    }


    public static Map<String, String> getMinio() {
        MinioProperties minioProperties = CommonBeanFactory.getBean(MinioProperties.class);
        Map<String, String> minioPros = new HashMap<>();
        assert minioProperties != null;
        minioPros.put("endpoint", minioProperties.getEndpoint());
        minioPros.put("accessKey", minioProperties.getAccessKey());
        minioPros.put("secretKey", minioProperties.getSecretKey());
        return minioPros;
    }

    /**
     * 获取当前项目配置的接口默认资源池
     *
     * @param projectId      项目ID
     * @param resourcePoolId 资源池ID
     */
    public TestResourcePoolReturnDTO getAvailableResourcePoolDTO(String projectId, String resourcePoolId) {
        TestResourcePool testResourcePool = testResourcePoolService.getTestResourcePool(resourcePoolId);
        if (testResourcePool == null ||
                // 资源池禁用
                !testResourcePool.getEnable() ||
                // 项目没有资源池权限
                !commonProjectService.validateProjectResourcePool(testResourcePool, projectId)) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        return testResourcePoolService.getTestResourcePoolDetail(resourcePoolId);
    }

    public String getProjectApiResourcePoolId(String projectId) {
        // 查询接口默认资源池
        ProjectApplication resourcePoolConfig = projectApplicationService.getByType(projectId, ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        // 没有配置接口默认资源池
        Map<String, Object> configMap = new HashMap<>();
        if (resourcePoolConfig != null && StringUtils.isNotBlank(resourcePoolConfig.getTypeValue())) {
            configMap.put(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), resourcePoolConfig.getTypeValue());
        }
        projectApplicationService.putResourcePool(projectId, configMap, "apiTest");
        if (MapUtils.isEmpty(configMap)) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        return (String) configMap.get(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
    }

    public void downloadFile(String reportId, String testId, FileRequest fileRequest, HttpServletResponse response) throws Exception {
        String key = getScriptRedisKey(reportId, testId);
        if (BooleanUtils.isTrue(stringRedisTemplate.hasKey(key))) {
            FileRepository repository = StringUtils.isBlank(fileRequest.getStorage()) ? FileCenter.getDefaultRepository()
                    : FileCenter.getRepository(fileRequest.getStorage());
            write2Response(repository.getFileAsStream(fileRequest), response);
        }
    }

    public void write2Response(InputStream in, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try (OutputStream out = response.getOutputStream()) {
            int len;
            byte[] bytes = new byte[1024 * 2];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            LogUtils.error(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LogUtils.error(e);
            }
        }
    }

    /**
     * 单接口执行
     *
     * @param runRequest
     * @return
     */
    public TaskRequestDTO apiExecute(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest, ApiParamConfig apiParamConfig) {
        // 设置使用脚本前后置的公共脚本信息
        AbstractMsTestElement testElement = runRequest.getTestElement();
        apiCommonService.setEnableCommonScriptProcessorInfo(testElement);
        testElement.setResourceId(taskRequest.getResourceId());
        testElement.setStepId(taskRequest.getResourceId());
        testElement.setProjectId(taskRequest.getProjectId());
        return execute(runRequest, taskRequest, apiParamConfig);
    }

    public ApiParamConfig getApiParamConfig(String reportId) {
        ApiParamConfig paramConfig = new ApiParamConfig();
        paramConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        paramConfig.setTestElementClassProtocalMap(apiPluginService.getTestElementProtocolMap());
        paramConfig.setReportId(reportId);
        return paramConfig;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        taskRequest.setReportId(reportId);
        taskRequest.setResourceId(resourceId);
        taskRequest.setProjectId(projectId);
        return taskRequest;
    }

    public String getDebugRunModule(boolean isFrontendDebug) {
        return isFrontendDebug ? ApiExecuteRunMode.FRONTEND_DEBUG.name() : ApiExecuteRunMode.BACKEND_DEBUG.name();
    }

    public ApiResourceRunRequest getApiResourceRunRequest(ApiDebugRunRequest request) {
        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        runRequest.setLinkFileIds(request.getLinkFileIds());
        runRequest.setUploadFileIds(request.getUploadFileIds());
        runRequest.setTestElement(ApiDataUtils.parseObject(JSON.toJSONString(request.getRequest()), AbstractMsTestElement.class));
        return runRequest;
    }
}