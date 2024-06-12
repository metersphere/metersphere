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
import io.metersphere.engine.MsHttpClient;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.service.*;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.exception.TaskRunnerResultCode;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.config.MinioProperties;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.service.*;
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
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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

    public String getTaskKey(String reportId, String testId) {
        return reportId + "_" + testId;
    }

    public TaskRequestDTO execute(ApiResourceRunRequest runRequest, TaskRequestDTO taskRequest, ApiParamConfig parameterConfig) {
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();

        // 解析执行脚本
        String executeScript = parseExecuteScript(runRequest.getTestElement(), parameterConfig);
        // 脚本已经解析，不需要再解析
        taskInfo.setNeedParseScript(false);

        // 将测试脚本缓存到 redis
        String scriptRedisKey = getTaskKey(taskItem.getReportId(), taskItem.getResourceId());
        stringRedisTemplate.opsForValue().set(scriptRedisKey, executeScript, 1, TimeUnit.DAYS);

        setTaskItemFileParam(runRequest, taskItem);

        if (StringUtils.equals(taskInfo.getRunMode(), ApiExecuteRunMode.FRONTEND_DEBUG.name())) {
            taskInfo = setTaskRequestParams(taskInfo);
            // 清空mino和kafka配置信息，避免前端获取
            taskInfo.setMinioConfig(null);
            taskInfo.setKafkaConfig(null);
            // 前端调试返回执行参数，由前端调用本地资源池执行
            return taskRequest;
        }

        try {
            return execute(taskRequest);
        } catch (Exception e) {
            // 调用失败清理脚本
            stringRedisTemplate.delete(scriptRedisKey);
            throw e;
        }
    }

    private TaskInfo setTaskRequestParams(TaskInfo taskInfo) {
        // 置minio kafka ms 等信息
        setServerInfoParam(taskInfo);
        // 设置项目执行所需要的文件信息
        setProjectTaskFileParam(taskInfo);

        // 误报处理
        taskInfo.setMsRegexList(projectApplicationService.get(Collections.singletonList(taskInfo.getProjectId())));

        return taskInfo;
    }

    /**
     * 给 taskRequest 设置当前项目相关的文件信息
     *
     * @param taskInfo 执行参数
     */
    private void setProjectTaskFileParam(TaskInfo taskInfo) {
        // 设置jar包信息
        setTaskFuncJarParam(taskInfo);
        // 设置插件文件信息
        setProjectPluginParam(taskInfo);
    }

    private void setProjectPluginParam(TaskInfo taskInfo) {
        // 设置插件文件信息
        CopyOnWriteArrayList<ApiExecuteFileInfo> pluginFiles = new CopyOnWriteArrayList<>();
        pluginFiles.addAll(apiPluginService.getFileInfoByProjectId(taskInfo.getProjectId()));
        pluginFiles.addAll(jdbcDriverPluginService.getFileInfoByProjectId(taskInfo.getProjectId()));
        taskInfo.getProjectResource().setPluginFiles(pluginFiles);
    }

    public GlobalParams getGlobalParam(String projectId) {
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
    public TaskRequestDTO execute(TaskRequestDTO taskRequest) {
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        TaskItem taskItem = taskRequest.getTaskItem();

        try {

            taskInfo = setTaskRequestParams(taskInfo);

            // 获取资源池
            TestResourcePoolReturnDTO testResourcePoolDTO = getGetResourcePoolNodeDTO(taskInfo.getRunModeConfig(), taskInfo.getProjectId());

            TestResourceNodeDTO testResourceNodeDTO = getNextExecuteNode(testResourcePoolDTO);

            if (StringUtils.isNotBlank(testResourcePoolDTO.getServerUrl())) {
                // 如果资源池配置了当前站点，则使用资源池的
                taskInfo.setMsUrl(testResourcePoolDTO.getServerUrl());
            }
            taskInfo.setPoolSize(testResourceNodeDTO.getConcurrentNumber());
            String endpoint = MsHttpClient.getEndpoint(testResourceNodeDTO.getIp(), testResourceNodeDTO.getPort());
            LogUtils.info("开始发送请求【 {}_{} 】到 {} 节点执行", taskItem.getReportId(), taskItem.getResourceId(), endpoint);
            if (StringUtils.equalsAny(taskInfo.getRunMode(), ApiExecuteRunMode.FRONTEND_DEBUG.name(), ApiExecuteRunMode.BACKEND_DEBUG.name())) {
                MsHttpClient.debugApi(endpoint, taskRequest);
            } else {
                MsHttpClient.runApi(endpoint, taskRequest);
            }

        } catch (HttpServerErrorException e) {
            LogUtils.error(e);
            int errorCode = e.getResponseBodyAs(ResultHolder.class).getCode();
            for (TaskRunnerResultCode taskRunnerResultCode : TaskRunnerResultCode.values()) {
                // 匹配资源池的错误代码，抛出相应异常
                if (taskRunnerResultCode.getCode() == errorCode) {
                    throw new MSException(taskRunnerResultCode, e.getMessage());
                }
            }
            throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
        } catch (MSException e) {
            LogUtils.error(e);
            throw e;
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
        }

        // 清空mino和kafka配置信息，避免前端获取
        taskInfo.setMinioConfig(null);
        taskInfo.setKafkaConfig(null);

        return taskRequest;
    }

    /**
     * 发送执行任务
     */
    public void batchExecute(TaskBatchRequestDTO taskRequest) {
        setTaskRequestParams(taskRequest.getTaskInfo());

        TaskInfo taskInfo = taskRequest.getTaskInfo();
        // 获取资源池
        TestResourcePoolReturnDTO testResourcePool = getGetResourcePoolNodeDTO(taskInfo.getRunModeConfig(), taskInfo.getProjectId());

        if (StringUtils.isNotBlank(testResourcePool.getServerUrl())) {
            // 如果资源池配置了当前站点，则使用资源池的
            taskInfo.setMsUrl(testResourcePool.getServerUrl());
        }

        // 将任务按资源池的数量拆分
        List<TestResourceNodeDTO> nodesList = testResourcePool.getTestResourceReturnDTO().getNodesList();
        List<TaskBatchRequestDTO> distributeTasks = new ArrayList<>(nodesList.size());
        for (int i = 0; i < taskRequest.getTaskItems().size(); i++) {
            TaskBatchRequestDTO distributeTask;
            int nodeIndex = i % nodesList.size();
            if (distributeTasks.size() < nodesList.size()) {
                distributeTask = BeanUtils.copyBean(new TaskBatchRequestDTO(), taskRequest);
                distributeTask.setTaskItems(new ArrayList<>());
                distributeTasks.add(distributeTask);
            } else {
                distributeTask = distributeTasks.get(nodeIndex);
            }
            distributeTask.getTaskInfo().setPoolSize(nodesList.get(nodeIndex).getConcurrentNumber());
            distributeTask.getTaskItems().add(taskRequest.getTaskItems().get(i));
        }

        for (int i = 0; i < nodesList.size(); i++) {
            // todo 优化某个资源池不可用的情况，以及清理 executionSet
            TestResourceNodeDTO testResourceNode = nodesList.get(i);
            TaskBatchRequestDTO subTaskRequest = distributeTasks.get(i);
            String endpoint = MsHttpClient.getEndpoint(testResourceNode.getIp(), testResourceNode.getPort());
            try {
                List<String> taskKeys = subTaskRequest.getTaskItems().stream()
                        .map(taskItem -> taskItem.getReportId() + "_" + taskItem.getResourceId())
                        .toList();
                LogUtils.info("开始发送批量任务到 {} 节点执行:\n" + taskKeys, endpoint);

                MsHttpClient.batchRunApi(endpoint, subTaskRequest);
            } catch (Exception e) {
                LogUtils.error("发送批量任务到 {} 节点执行失败", endpoint);
                LogUtils.error(e);
            }
        }
    }

    protected static boolean validate() {
        try {
            LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
            return Optional.ofNullable(licenseService)
                    .map(LicenseService::validate)
                    .map(dto -> StringUtils.equals(dto.getStatus(), "valid"))
                    .orElse(false);

        } catch (Exception e) {
            return false;
        }
    }

    private TestResourceNodeDTO getNextExecuteNode(TestResourcePoolReturnDTO resourcePoolDTO) {
        if (!validate()) {
            return resourcePoolDTO.getTestResourceReturnDTO().getNodesList().getFirst();
        }

        roundRobinService.initializeNodes(resourcePoolDTO.getId(), resourcePoolDTO.getTestResourceReturnDTO().getNodesList());
        try {
            TestResourceNodeDTO node = roundRobinService.getNextNode(resourcePoolDTO.getId());
            if (node == null) {
                node = resourcePoolDTO.getTestResourceReturnDTO().getNodesList().getFirst();
            }
            return node;
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("get execute node error", e);
        }
    }

    public TestResourcePoolReturnDTO getGetResourcePoolNodeDTO(ApiRunModeConfigDTO runModeConfig, String projectId) {
        String poolId = runModeConfig.getPoolId();
        if (StringUtils.isBlank(poolId)) {
            poolId = getProjectApiResourcePoolId(projectId);
        }
        TestResourcePoolReturnDTO resourcePool = getAvailableResourcePoolDTO(projectId, poolId);

        if (resourcePool == null || CollectionUtils.isEmpty(resourcePool.getTestResourceReturnDTO().getNodesList())) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        return resourcePool;
    }

    /**
     * 设置minio kafka ms 等信息
     *
     * @param taskInfo 执行参数
     */
    private void setServerInfoParam(TaskInfo taskInfo) {
        taskInfo.setKafkaConfig(JSON.toJSONString(KafkaConfig.getKafkaConfig()));
        taskInfo.setMinioConfig(JSON.toJSONString(getMinio()));
        taskInfo.setMsUrl(systemParameterService.getBaseInfo().getUrl());
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
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        setServerInfoParam(taskInfo);
        taskInfo.setRealTime(true);
        taskInfo.setSaveResult(false);
        taskInfo.setResourceType(ApiExecuteResourceType.API_DEBUG.name());
        taskInfo.setRunMode(ApiExecuteRunMode.BACKEND_DEBUG.name());

        return execute(apiRunRequest, taskRequest, new ApiParamConfig());
    }

    /**
     * 给 taskRequest 设置文件相关参数
     *
     * @param runRequest 请求参数
     */
    public void setTaskItemFileParam(ApiResourceRunRequest runRequest, TaskItem taskItem) {
        // 接口执行相关的文件
        setTaskRefFileParam(runRequest, taskItem.getTaskResourceFile(), taskItem.getResourceId());
        // 调试时未保存的文件
        setTaskTmpFileParam(runRequest, taskItem.getTaskResourceFile());
        // 场景引用跨项目的用例所需要的jar包
        setTaskFuncJarParam(taskItem, runRequest.getRefProjectIds().stream().toList());
    }

    /**
     * 给 taskRequest 设置文件相关参数
     */
    public void setTaskItemFileParam(TaskItem taskItem) {
        setTaskItemFileParam(new ApiResourceRunRequest(), taskItem);
    }

    /**
     * 处理脚本执行所需要的jar包
     *
     * @param taskInfo
     */
    private void setTaskFuncJarParam(TaskInfo taskInfo) {
        // 获取函数jar包
        List<FileMetadata> fileMetadataList = fileManagementService.findJarByProjectId(List.of(taskInfo.getProjectId()));
        taskInfo.getProjectResource().setFuncJars(getApiExecuteFileInfo(fileMetadataList));

        // TODO 当前项目没有包分两种情况，1 之前存在被删除，2 一直不存在
        //  为了兼容1 这种情况需要初始化一条空的数据，由执行机去做卸载
        if (CollectionUtils.isEmpty(taskInfo.getProjectResource().getFuncJars())) {
            ApiExecuteFileInfo tempFileInfo = new ApiExecuteFileInfo();
            tempFileInfo.setProjectId(taskInfo.getProjectId());
            CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
            copyOnWriteArrayList.add(tempFileInfo);
            taskInfo.getProjectResource().setFuncJars(copyOnWriteArrayList);
        }
    }

    /**
     * 处理脚本执行所需要的jar包
     *
     * @param taskItem
     */
    private void setTaskFuncJarParam(TaskItem taskItem, List<String> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        TaskProjectResource projectResource = taskItem.getRefProjectResource();
        // 获取函数jar包
        List<FileMetadata> fileMetadataList = fileManagementService.findJarByProjectId(projectIds);
        projectResource.setFuncJars(getApiExecuteFileInfo(fileMetadataList));
    }

    /**
     * 处理没有保存的临时文件
     *
     * @param runRequest
     */
    private void setTaskTmpFileParam(ApiResourceRunRequest runRequest, TaskResourceFile taskResourceFile) {
        // 没有保存的本地临时文件
        List<String> uploadFileIds = runRequest.getUploadFileIds();
        if (CollectionUtils.isNotEmpty(uploadFileIds)) {
            List<ApiExecuteFileInfo> localTempFiles = uploadFileIds.stream()
                    .map(tempFileId -> {
                        String fileName = apiFileResourceService.getTempFileNameByFileId(tempFileId);
                        return getApiExecuteFileInfo(tempFileId, fileName, null);
                    })
                    // uploadFileIds 查不到，则已经移动到正式目录
                    .filter(i -> StringUtils.isNotBlank(i.getFileName()))
                    .collect(Collectors.toList());
            taskResourceFile.setLocalTempFiles(localTempFiles);
        }

        List<String> linkFileIds = runRequest.getLinkFileIds();
        // 没有保存的文件管理临时文件
        if (CollectionUtils.isNotEmpty(linkFileIds)) {
            List<FileMetadata> fileMetadataList = fileMetadataService.getByFileIds(linkFileIds);
            // 添加临时的文件管理的文件
            taskResourceFile.getRefFiles().addAll(getApiExecuteFileInfo(fileMetadataList));
        }
    }

    /**
     * 处理运行的资源所关联的文件信息
     *
     * @param runRequest
     */
    private void setTaskRefFileParam(ApiResourceRunRequest runRequest, TaskResourceFile taskResourceFile, String resourceId) {
        // 查询包括资源所需的文件
        Set<String> resourceIdsSet = runRequest.getFileResourceIds();
        resourceIdsSet.add(resourceId);
        List<String> resourceIds = resourceIdsSet.stream().collect(Collectors.toList());
        SubListUtils.dealForSubList(resourceIds, 50, subResourceIds -> {
            // 查询通过本地上传的文件
            List<ApiExecuteFileInfo> localFiles = apiFileResourceService.getByResourceIds(subResourceIds).
                    stream()
                    .map(file -> {
                        ApiExecuteFileInfo apiExecuteFileInfo = getApiExecuteFileInfo(file.getFileId(), file.getFileName(), file.getProjectId());
                        // 本地上传的文件需要 resourceId 查询对应的目录
                        apiExecuteFileInfo.setResourceId(file.getResourceId());
                        apiExecuteFileInfo.setResourceType(file.getResourceType());
                        if (StringUtils.equals(file.getResourceType(), ApiFileResourceType.API_SCENARIO_STEP.name())) {
                            apiExecuteFileInfo.setScenarioId(runRequest.getFileStepScenarioMap().get(file.getResourceId()));
                        }
                        return apiExecuteFileInfo;
                    })
                    .collect(Collectors.toList());
            taskResourceFile.setLocalFiles(localFiles);

            // 查询关联的文件管理的文件
            List<ApiExecuteFileInfo> refFiles = fileAssociationService.getFiles(subResourceIds).
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
            taskResourceFile.setRefFiles(refFiles);
        });
    }

    private CopyOnWriteArrayList<ApiExecuteFileInfo> getApiExecuteFileInfo(List<FileMetadata> fileMetadataList) {
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
                }).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
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
    public String parseExecuteScript(AbstractMsTestElement msTestElement, ParameterConfig config) {
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
        String key = getTaskKey(reportId, testId);
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
        setTestElementParam(runRequest.getTestElement(), taskRequest.getTaskInfo().getProjectId(), taskRequest.getTaskItem());
        return execute(runRequest, taskRequest, apiParamConfig);
    }

    public void setTestElementParam(AbstractMsTestElement testElement, String projectId, TaskItem taskItem) {
        apiCommonService.setEnableCommonScriptProcessorInfo(testElement);
        testElement.setResourceId(taskItem.getResourceId());
        testElement.setStepId(taskItem.getResourceId());
        testElement.setProjectId(projectId);
    }

    public ApiParamConfig getApiParamConfig(String reportId) {
        ApiParamConfig paramConfig = new ApiParamConfig();
        paramConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        paramConfig.setTestElementClassProtocolMap(apiPluginService.getTestElementProtocolMap());
        paramConfig.setReportId(reportId);
        return paramConfig;
    }

    public ApiParamConfig getApiParamConfig(String reportId, String projectId) {
        ApiParamConfig paramConfig = new ApiParamConfig();
        paramConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        paramConfig.setTestElementClassProtocolMap(apiPluginService.getTestElementProtocolMap());
        paramConfig.setReportId(reportId);
        paramConfig.setGlobalParams(getGlobalParam(projectId));
        return paramConfig;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId) {
        TaskRequestDTO taskRequest = new TaskRequestDTO();
        TaskInfo taskInfo = getTaskInfo(projectId);
        TaskItem taskItem = getTaskItem(reportId, resourceId);
        taskRequest.setTaskInfo(taskInfo);
        taskRequest.setTaskItem(taskItem);
        return taskRequest;
    }

    public TaskInfo getTaskInfo(String projectId) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setProjectId(projectId);
        return taskInfo;
    }

    public TaskItem getTaskItem(String reportId, String resourceId) {
        TaskItem taskItem = new TaskItem();
        taskItem.setReportId(reportId);
        taskItem.setResourceId(resourceId);
        return taskItem;
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