package io.metersphere.api.service;

import io.metersphere.api.config.JmeterProperties;
import io.metersphere.api.config.KafkaConfig;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileManagementService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.config.MinioProperties;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.utils.TaskRunnerClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private JmeterProperties jmeterProperties;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileManagementService fileManagementService;


    @PostConstruct
    private void init() {
        String jmeterHome = getJmeterHome();
        JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter.properties");
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public String getJmeterHome() {
        String home = getClass().getResource("/").getPath() + "jmeter";
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

    public void debug(ApiResourceRunRequest request) {
        TestResourceDTO resourcePoolDTO = getAvailableResourcePoolDTO(request.getProjectId());
        String reportId = request.getReportId();
        String testId = request.getTestId();

        TaskRequest taskRequest = new TaskRequest();
        BeanUtils.copyBean(taskRequest, request);
        taskRequest.setKafkaConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(KafkaConfig.getKafkaConfig())));
        taskRequest.setMinioConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(getMinio())));
        taskRequest.setMsUrl(systemParameterService.getBaseInfo().getUrl());
        taskRequest.setRealTime(true);

        // 设置执行文件参数
        setTaskFileParam(request, taskRequest);

//         todo 环境配置
//        EnvironmentInfoDTO environmentInfoDTO = environmentService.get(request.getEnvironmentId());
        // todo 误报
        // todo 获取接口插件和jar包
        // todo 处理公共脚本
        // todo 接口用例 method 获取定义中的数据库字段
        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setReportId(reportId);
        String executeScript = parseExecuteScript(request.getRequest(), parameterConfig);

        // 将测试脚本缓存到 redis
        String scriptRedisKey = getScriptRedisKey(reportId, testId);
        stringRedisTemplate.opsForValue().set(scriptRedisKey, executeScript);

        List<TestResourceNodeDTO> nodesList = resourcePoolDTO.getNodesList();
        int index = new SecureRandom().nextInt(nodesList.size());
        TestResourceNodeDTO testResourceNodeDTO = nodesList.get(index);
        String endpoint = TaskRunnerClient.getEndpoint(testResourceNodeDTO.getIp(), testResourceNodeDTO.getPort());
        try {
            LogUtils.info(String.format("开始发送请求【 %s 】到 %s 节点执行", testId, endpoint), reportId);
            TaskRunnerClient.debugApi(endpoint, taskRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            // 调用失败清理脚本
            stringRedisTemplate.delete(scriptRedisKey);
            throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
        }
    }

    /**
     * 给 taskRequest 设置文件相关参数
     *
     * @param request
     * @param taskRequest
     */
    private void setTaskFileParam(ApiResourceRunRequest request, TaskRequest taskRequest) {
        // 查询通过本地上传的文件
        List<ApiExecuteFileInfo> localFiles = apiFileResourceService.getByResourceId(request.getId()).
                stream()
                .map(file -> {
                    ApiExecuteFileInfo apiExecuteFileInfo = getApiExecuteFileInfo(file.getFileId(), file.getFileName(), file.getProjectId());
                    // 本地上传的文件需要 resourceId 查询对应的目录
                    apiExecuteFileInfo.setResourceId(request.getId());
                    return apiExecuteFileInfo;
                })
                .collect(Collectors.toList());
        taskRequest.setLocalFiles(localFiles);

        // 查询关联的文件管理的文件
        List<ApiExecuteFileInfo> refFiles = fileAssociationService.getFiles(request.getId()).
                stream()
                .map(file -> {
                    ApiExecuteFileInfo refFileInfo = getApiExecuteFileInfo(file.getFileId(), file.getFileName(),
                            file.getProjectId(), file.getStorage());
                    if (StorageType.isGit(file.getStorage())) {
                        // 设置Git信息
                        refFileInfo.setFileMetadataRepositoryDTO(fileManagementService.getFileMetadataRepositoryDTO(file.getMetadataId()));
                        refFileInfo.setFileModuleRepositoryDTO(fileManagementService.getFileModuleRepositoryDTO(file.getModuleId()));
                    }
                    return refFileInfo;
                }).collect(Collectors.toList());

        // 处理没有保存的临时文件
        List<String> tempFileIds = request.getTempFileIds();
        if (CollectionUtils.isNotEmpty(tempFileIds)) {
            // 查询这些文件有哪些是关联文件管理的文件
            List<ApiExecuteFileInfo> refTempFiles = fileMetadataService.getByFileIds(tempFileIds)
                    .stream()
                    .map(file -> {
                        String fileName = file.getName();
                        if (StringUtils.isNotBlank(file.getType())) {
                            fileName += "." + file.getType();
                        }
                        ApiExecuteFileInfo tempFileInfo = getApiExecuteFileInfo(file.getId(), fileName,
                                file.getProjectId(), file.getStorage());
                        if (StorageType.isGit(file.getStorage())) {
                            // 设置Git信息
                            tempFileInfo.setFileMetadataRepositoryDTO(fileManagementService.getFileMetadataRepositoryDTO(file.getId()));
                            tempFileInfo.setFileModuleRepositoryDTO(fileManagementService.getFileModuleRepositoryDTO(file.getModuleId()));
                        }
                        return tempFileInfo;
                    }).collect(Collectors.toList());
            // 添加临时的文件管理的文件
            refFiles.addAll(refTempFiles);

            Set<String> refTempFileIds = refTempFiles.stream().map(ApiExecuteFileInfo::getFileId).collect(Collectors.toSet());
            // 去掉文件管理的文件，即通过本地上传的临时文件
            List<ApiExecuteFileInfo> localTempFiles = tempFileIds.stream()
                    .filter(tempFileId -> !refTempFileIds.contains(tempFileId))
                    .map(tempFileId -> {
                        String fileName = apiFileResourceService.getTempFileNameByFileId(tempFileId);
                        ApiExecuteFileInfo apiExecuteFileInfo = getApiExecuteFileInfo(tempFileId, fileName, request.getProjectId());
                        return apiExecuteFileInfo;
                    })
                    .collect(Collectors.toList());
            taskRequest.setLocalTempFiles(localTempFiles);
        }

        taskRequest.setRefFiles(refFiles);
    }

    private static ApiExecuteFileInfo getApiExecuteFileInfo(String fileId, String fileName, String projectId) {
        return getApiExecuteFileInfo(fileId, fileName, projectId, StorageType.MINIO.name());
    }

    private static ApiExecuteFileInfo getApiExecuteFileInfo(String fileId, String fileName, String projectId, String storage) {
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
     * @param testElementStr
     * @param msParameter
     * @return
     */
    private static String parseExecuteScript(String testElementStr, ParameterConfig msParameter) {
        // 解析生成脚本
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(testElementStr, AbstractMsTestElement.class);
        return defaultParser.parse(msTestElement, msParameter);
    }


    public static Map<String, String> getMinio() {
        MinioProperties minioProperties = CommonBeanFactory.getBean(MinioProperties.class);
        Map<String, String> minioPros = new HashMap<>();
        minioPros.put("endpoint", minioProperties.getEndpoint());
        minioPros.put("accessKey", minioProperties.getAccessKey());
        minioPros.put("secretKey", minioProperties.getSecretKey());
        return minioPros;
    }

    /**
     * 获取当前项目配置的接口默认资源池
     *
     * @param projectId
     * @param
     */
    public TestResourceDTO getAvailableResourcePoolDTO(String projectId) {
        // 查询接口默认资源池
        ProjectApplication resourcePoolConfig = projectApplicationService.getByType(projectId, ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        // 没有配置接口默认资源池
        if (resourcePoolConfig == null || StringUtils.isBlank(resourcePoolConfig.getTypeValue())) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        String resourcePoolId = StringUtils.isBlank(resourcePoolConfig.getTypeValue()) ? null : resourcePoolConfig.getTypeValue();
        TestResourcePool testResourcePool = testResourcePoolService.getTestResourcePool(resourcePoolId);
        if (testResourcePool == null ||
                // 资源池禁用
                !testResourcePool.getEnable() ||
                // 项目没有资源池权限
                !commonProjectService.validateProjectResourcePool(testResourcePool, projectId)) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        return testResourcePoolService.getTestResourceDTO(resourcePoolId);
    }
}