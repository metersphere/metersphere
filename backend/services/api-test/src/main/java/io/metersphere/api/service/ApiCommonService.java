package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.dto.ApiDefinitionExecuteInfo;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.ResponseBinaryBody;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.MsEnvAssertionConfig;
import io.metersphere.project.dto.environment.processors.*;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.TaskItemErrorMessage;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-20  21:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCommonService {
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private CustomFunctionService customFunctionService;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ExecTaskMapper execTaskMapper;

    /**
     * 根据 fileId 查找 MsHTTPElement 中的 ApiFile
     *
     * @param fileId
     * @param msTestElement
     * @return
     */
    public List<ApiFile> getApiFilesByFileId(String fileId, AbstractMsTestElement msTestElement) {
        if (msTestElement instanceof MsHTTPElement httpElement) {
            List<ApiFile> apiFiles = getApiBodyFiles(httpElement.getBody());
            return apiFiles.stream()
                    .filter(file -> StringUtils.equals(fileId, file.getFileId()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<ApiFile> getApiFiles(AbstractMsTestElement msTestElement) {
        if (msTestElement instanceof MsHTTPElement httpElement) {
            return getApiBodyFiles(httpElement.getBody());
        } else {
            return List.of();
        }
    }

    /**
     * 设置关联的文件的最新信息
     * 包括文件别名和是否被删除
     *
     * @param resourceId
     * @param msTestElement
     */
    public void setLinkFileInfo(String resourceId, AbstractMsTestElement msTestElement) {
        setLinkFileInfo(resourceId, getApiFiles(msTestElement));
    }

    /**
     * 设置关联的文件的最新信息
     * 包括文件别名和是否被删除
     *
     * @param resourceId
     * @param responseBody
     */
    public void setLinkFileInfo(String resourceId, ResponseBody responseBody) {
        setLinkFileInfo(resourceId, getApiBodyFiles(responseBody));
    }

    public void setLinkFileInfo(String resourceId, List<ApiFile> apiFiles) {
        List<ApiFile> linkFiles = apiFiles.stream()
                .filter(file -> {
                    if (file.getLocal()) {
                        // 本地文件设置文件别名，方便前端展示
                        file.setFileAlias(file.getFileName());
                    }
                    return !file.getLocal() && !file.getDelete();
                })
                .toList();
        List<String> linkFileIds = linkFiles.stream()
                .map(ApiFile::getFileId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(linkFileIds)) {
            return;
        }

        Map<String, FileMetadata> fileNameMap = fileMetadataService.selectByList(linkFileIds)
                .stream()
                .collect(Collectors.toMap(FileMetadata::getId, Function.identity()));

        for (ApiFile linkFile : linkFiles) {
            FileMetadata fileMetadata = fileNameMap.get(linkFile.getFileId());
            if (fileMetadata == null) {
                // fileMetadata null ，则文件被删除，设置为已删除，并且设置文件名
                linkFile.setDelete(true);
                List<FileAssociation> fileAssociations = fileAssociationService.getByFileIdAndSourceId(resourceId, linkFile.getFileId());
                if (CollectionUtils.isNotEmpty(fileAssociations)) {
                    linkFile.setFileAlias(fileAssociations.getFirst().getDeletedFileName());
                }
            } else {
                String fileName = fileMetadata.getName();
                if (StringUtils.isNotBlank(fileMetadata.getType())) {
                    // 前端展示别名加后缀
                    fileName += "." + fileMetadata.getType();
                }
                linkFile.setFileAlias(fileName);
            }
        }
    }


    /**
     * @param body
     * @return
     */
    public List<ApiFile> getApiBodyFiles(Body body) {
        List<ApiFile> updateFiles = new ArrayList<>(0);
        if (body != null) {
            FormDataBody formDataBody = body.getFormDataBody();
            if (formDataBody != null) {
                List<FormDataKV> formValues = formDataBody.getFormValues();
                if (CollectionUtils.isNotEmpty(formValues)) {
                    formValues.forEach(formDataKV -> {
                        List<ApiFile> files = formDataKV.getFiles();
                        if (CollectionUtils.isNotEmpty(files)) {
                            updateFiles.addAll(files);
                        }
                    });
                }
            }
            BinaryBody binaryBody = body.getBinaryBody();
            if (binaryBody != null && binaryBody.getFile() != null) {
                updateFiles.add(binaryBody.getFile());
            }
        }
        return updateFiles;
    }

    public List<ApiFile> getApiBodyFiles(ResponseBody responseBody) {
        List<ApiFile> updateFiles = new ArrayList<>(0);
        if (responseBody != null) {
            ResponseBinaryBody binaryBody = responseBody.getBinaryBody();
            if (binaryBody != null && binaryBody.getFile() != null) {
                updateFiles.add(binaryBody.getFile());
            }
        }
        return updateFiles;
    }

    public void replaceApiFileInfo(List<ApiFile> updateFiles, FileMetadata newFileMetadata) {
        for (ApiFile updateFile : updateFiles) {
            updateFile.setFileId(newFileMetadata.getId());
            updateFile.setFileName(newFileMetadata.getOriginalName());
        }
    }

    /**
     * 设置使用脚本前后置的公共脚本信息
     *
     * @param msTestElement
     */
    public void setEnableCommonScriptProcessorInfo(AbstractMsTestElement msTestElement) {
        MsCommonElement msCommonElement = getMsCommonElement(msTestElement);
        Optional.ofNullable(msCommonElement).ifPresent(item -> setCommonElementEnableCommonScriptInfo(List.of(item)));
    }

    /**
     * 设置使用脚本步骤的公共脚本信息
     *
     * @param msScriptElement
     */
    public void setEnableCommonScriptProcessorInfo(MsScriptElement msScriptElement) {
        CommonScriptInfo commonScriptInfo = msScriptElement.getCommonScriptInfo();
        if (BooleanUtils.isTrue(msScriptElement.getEnableCommonScript()) && commonScriptInfo != null) {
            setEnableCommonScriptInfo(List.of(commonScriptInfo));
        }
    }

    /**
     * 设置使用脚本步骤的公共脚本信息
     *
     * @param msScriptElements
     */
    public void setScriptElementEnableCommonScriptInfo(List<MsScriptElement> msScriptElements) {
        List<CommonScriptInfo> commonScriptInfos = msScriptElements.stream()
                .filter(msScriptElement -> BooleanUtils.isTrue(msScriptElement.getEnableCommonScript()))
                .map(MsScriptElement::getCommonScriptInfo)
                .toList();

        setEnableCommonScriptInfo(commonScriptInfos);
    }

    /**
     * 设置使用脚本前后置的公共脚本信息
     *
     * @param commonElements
     */
    public void setCommonElementEnableCommonScriptInfo(List<MsCommonElement> commonElements) {
        List<ScriptProcessor> scriptsProcessors = getEnableCommonScriptProcessors(commonElements);
        List<MsScriptAssertion> scriptAssertions = getEnableCommonScriptAssertion(commonElements);

        List<CommonScriptInfo> commonScriptInfos = scriptsProcessors.stream()
                .map(ScriptProcessor::getCommonScriptInfo).collect(Collectors.toList());

        List<CommonScriptInfo> assertionsCommonScriptInfos = scriptAssertions.stream()
                .map(MsScriptAssertion::getCommonScriptInfo).toList();

        commonScriptInfos.addAll(assertionsCommonScriptInfos);

        setEnableCommonScriptInfo(commonScriptInfos);
    }

    /**
     * 设置环境前后置的公共脚本信息
     *
     * @param envConfig
     */
    public void setEnvCommonScriptInfo(EnvironmentInfoDTO envConfig) {
        if (envConfig == null || envConfig.getConfig() == null) {
            return;
        }
        try {
            // 获取脚本
            List<ScriptProcessor> scriptsProcessors = getEnableCommonScriptProcessors(envConfig.getConfig().getPreProcessorConfig());
            scriptsProcessors.addAll(getEnableCommonScriptProcessors(envConfig.getConfig().getPostProcessorConfig()));

            // 获取断言
            List<MsScriptAssertion> scriptAssertions = getEnableCommonScriptAssertion(envConfig.getConfig().getAssertionConfig());

            List<CommonScriptInfo> commonScriptInfos = scriptsProcessors.stream()
                    .map(ScriptProcessor::getCommonScriptInfo)
                    .collect(Collectors.toList());

            List<CommonScriptInfo> assertionsCommonScriptInfos = scriptAssertions.stream()
                    .map(MsScriptAssertion::getCommonScriptInfo)
                    .toList();

            commonScriptInfos.addAll(assertionsCommonScriptInfos);
            // 设置最新的公共脚本信息
            setEnableCommonScriptInfo(commonScriptInfos);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 获取环境使用公共脚本的前后置
     *
     * @param envProcessorConfig
     * @return
     */
    private List<ScriptProcessor> getEnableCommonScriptProcessors(EnvProcessorConfig envProcessorConfig) {
        if (envProcessorConfig == null) {
            return new ArrayList<>(0);
        }
        ApiEnvProcessorConfig apiProcessorConfig = envProcessorConfig.getApiProcessorConfig();
        ApiEnvPlanProcessorConfig planProcessorConfig = apiProcessorConfig.getPlanProcessorConfig();
        ApiEnvScenarioProcessorConfig scenarioProcessorConfig = apiProcessorConfig.getScenarioProcessorConfig();
        ApiEnvRequestProcessorConfig requestProcessorConfig = apiProcessorConfig.getRequestProcessorConfig();

        List<MsProcessor> processors = new ArrayList<>();
        processors.addAll(planProcessorConfig.getProcessors());
        processors.addAll(scenarioProcessorConfig.getProcessors());
        processors.addAll(requestProcessorConfig.getProcessors());

        // 获取使用公共脚本的前后置
        return processors.stream()
                .filter(processor -> processor instanceof ScriptProcessor)
                .map(processor -> (ScriptProcessor) processor)
                .filter(ScriptProcessor::isEnableCommonScript)
                .filter(ScriptProcessor::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 获取使用公共脚本的前后置
     *
     * @param envAssertionConfig
     * @return
     */
    private List<MsScriptAssertion> getEnableCommonScriptAssertion(MsEnvAssertionConfig envAssertionConfig) {
        if (envAssertionConfig == null || CollectionUtils.isEmpty(envAssertionConfig.getAssertions())) {
            return List.of();
        }
        return envAssertionConfig.getAssertions()
                .stream()
                .filter(assertion -> assertion instanceof MsScriptAssertion)
                .map(msAssertion -> (MsScriptAssertion) msAssertion)
                .filter(MsScriptAssertion::isEnableCommonScript)
                .filter(MsScriptAssertion::isValid)
                .toList();
    }

    private void setEnableCommonScriptInfo(List<CommonScriptInfo> commonScriptInfos) {
        List<String> commonScriptIds = commonScriptInfos.stream()
                .map(CommonScriptInfo::getId)
                .toList();

        Map<String, CustomFunctionBlob> customFunctionBlobMap = customFunctionService.getBlobByIds(commonScriptIds).stream()
                .collect(Collectors.toMap(CustomFunctionBlob::getId, Function.identity()));

        Map<String, CustomFunction> customFunctionMap = customFunctionService.getByIds(commonScriptIds).stream()
                .collect(Collectors.toMap(CustomFunction::getId, Function.identity()));

        for (CommonScriptInfo commonScriptInfo : commonScriptInfos) {
            CustomFunctionBlob customFunctionBlob = customFunctionBlobMap.get(commonScriptInfo.getId());

            CustomFunction customFunction = customFunctionMap.get(commonScriptInfo.getId());

            if (customFunction == null || customFunctionBlob == null) {
                if (customFunction == null) {
                    // 公共脚本被删除，就改成非公共脚本
                    commonScriptInfo.setDeleted(true);
                }
                return;
            }

            // 设置公共脚本信息
            Optional.ofNullable(customFunctionBlob.getParams()).ifPresent(paramsBlob -> {
                List<KeyValueParam> commonParams = JSON.parseArray(new String(paramsBlob), KeyValueParam.class);
                // 替换用户输入值
                commonParams.forEach(commonParam ->
                        Optional.ofNullable(commonScriptInfo.getParams()).flatMap(params -> params.stream()
                                .filter(param -> StringUtils.equals(commonParam.getKey(), param.getKey()))
                                .findFirst()).ifPresent(param -> commonParam.setValue(param.getValue()))
                );
                commonScriptInfo.setParams(commonParams);
            });
            Optional.ofNullable(customFunctionBlob.getScript()).ifPresent(script ->
                    commonScriptInfo.setScript(new String(script)));
            commonScriptInfo.setScriptLanguage(customFunction.getType());
            commonScriptInfo.setName(customFunction.getName());
        }
    }

    /**
     * 获取使用公共脚本的前后置
     *
     * @param commonElements
     * @return
     */
    private List<ScriptProcessor> getEnableCommonScriptProcessors(List<MsCommonElement> commonElements) {
        List<MsProcessor> processors = new ArrayList<>();

        for (MsCommonElement commonElement : commonElements) {
            if (commonElement.getPreProcessorConfig() == null) {
                continue;
            }
            processors.addAll(commonElement.getPreProcessorConfig().getProcessors());
            processors.addAll(commonElement.getPostProcessorConfig().getProcessors());
        }

        // 获取使用公共脚本的前后置
        return processors.stream()
                .filter(processor -> processor instanceof ScriptProcessor)
                .map(processor -> (ScriptProcessor) processor)
                .filter(ScriptProcessor::isEnableCommonScript)
                .filter(ScriptProcessor::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 获取使用公共脚本的前后置
     *
     * @param commonElements
     * @return
     */
    private List<MsScriptAssertion> getEnableCommonScriptAssertion(List<MsCommonElement> commonElements) {
        List<MsScriptAssertion> assertions = new ArrayList<>();

        for (MsCommonElement commonElement : commonElements) {
            if (commonElement.getAssertionConfig() == null) {
                continue;
            }
            List<MsScriptAssertion> scriptAssertions = commonElement.getAssertionConfig().getAssertions()
                    .stream()
                    .filter(assertion -> assertion instanceof MsScriptAssertion)
                    .map(msAssertion -> (MsScriptAssertion) msAssertion)
                    .filter(MsScriptAssertion::isEnableCommonScript)
                    .filter(MsScriptAssertion::isValid)
                    .toList();
            assertions.addAll(scriptAssertions);
        }
        return assertions;
    }

    public MsCommonElement getMsCommonElement(AbstractMsTestElement msTestElement) {
        if (CollectionUtils.isNotEmpty(msTestElement.getChildren())) {
            for (AbstractMsTestElement child : msTestElement.getChildren()) {
                if (child instanceof MsCommonElement msCommonElement) {
                    return msCommonElement;
                }
            }
        }
        return null;
    }

    /**
     * 获取资源 ID 和接口定义信息 的 Map
     *
     * @param getDefinitionInfoFunc
     * @param resourceIds
     * @return
     */
    public Map<String, ApiDefinitionExecuteInfo> getApiDefinitionExecuteInfoMap(Function<List<String>, List<ApiDefinitionExecuteInfo>> getDefinitionInfoFunc, List<String> resourceIds) {
        return getDefinitionInfoFunc.apply(resourceIds)
                .stream()
                .collect(Collectors.toMap(ApiDefinitionExecuteInfo::getResourceId, Function.identity()));
    }

    /**
     * 设置 MsHTTPElement 中的 method 等信息
     *
     * @param msTestElement
     * @param definitionExecuteInfo
     */
    public void setApiDefinitionExecuteInfo(AbstractMsTestElement msTestElement, ApiDefinitionExecuteInfo definitionExecuteInfo) {
        if (msTestElement instanceof MsHTTPElement httpElement && definitionExecuteInfo != null) {
            httpElement.setModuleId(definitionExecuteInfo.getModuleId());
            httpElement.setMethod(definitionExecuteInfo.getMethod());
            httpElement.setPath(definitionExecuteInfo.getPath());
            httpElement.setNum(definitionExecuteInfo.getNum());
        }
    }

    /**
     * 给 httpElement 设置接口定义参数
     *
     * @param apiDefinition
     * @param msTestElement
     */
    public void setApiDefinitionExecuteInfo(AbstractMsTestElement msTestElement, ApiDefinition apiDefinition) {
        setApiDefinitionExecuteInfo(msTestElement, BeanUtils.copyBean(new ApiDefinitionExecuteInfo(), apiDefinition));
    }

    public ExecTask newExecTask(String projectId, String userId) {
        ExecTask execTask = new ExecTask();
        execTask.setNum(NumGenerator.nextNum(ApplicationNumScope.TASK));
        execTask.setProjectId(projectId);
        execTask.setId(IDGenerator.nextStr());
        execTask.setCreateTime(System.currentTimeMillis());
        execTask.setCreateUser(userId);
        execTask.setStatus(ExecStatus.PENDING.name());
        return execTask;
    }

    public ExecTaskItem newExecTaskItem(String taskId, String projectId, String userId) {
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setCreateTime(System.currentTimeMillis());
        execTaskItem.setId(IDGenerator.nextStr());
        execTaskItem.setTaskId(taskId);
        execTaskItem.setProjectId(projectId);
        execTaskItem.setExecutor(userId);
        execTaskItem.setStatus(ExecStatus.PENDING.name());
        execTaskItem.setResourcePoolId(StringUtils.EMPTY);
        execTaskItem.setResourcePoolNode(StringUtils.EMPTY);
        return execTaskItem;
    }

    public ApiReportRelateTask getApiReportRelateTask(String taskItemId, String reportId) {
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(reportId);
        apiReportRelateTask.setTaskResourceId(taskItemId);
        return apiReportRelateTask;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void batchUpdateTaskItemErrorMassage(TaskItemErrorMessage errorMessage, TaskBatchRequestDTO batchRequest) {
        SqlSessionFactory sqlSessionFactory = CommonBeanFactory.getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            if (CollectionUtils.isNotEmpty(batchRequest.getTaskItems())) {
                ExecTaskItemMapper batchExecTaskItemMapper = sqlSession.getMapper(ExecTaskItemMapper.class);
                batchRequest.getTaskItems().forEach(taskItem -> {
                    // 更新任务项的异常信息
                    ExecTaskItem execTaskItem = new ExecTaskItem();
                    execTaskItem.setId(taskItem.getId());
                    execTaskItem.setErrorMessage(errorMessage.name());
                    batchExecTaskItemMapper.updateByPrimaryKeySelective(execTaskItem);
                });
            }
        } finally {
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void batchUpdateTaskItemErrorMassage(TaskItemErrorMessage errorMessage, TaskRequestDTO taskRequestDTO) {
        // 更新任务项的异常信息
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setId(taskRequestDTO.getTaskItem().getId());
        execTaskItem.setErrorMessage(errorMessage.name());
        execTaskItemMapper.updateByPrimaryKeySelective(execTaskItem);
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateTaskItemErrorMassage(String taskItemId, TaskItemErrorMessage errorMessage) {
        // 更新任务项的异常信息
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setId(taskItemId);
        execTaskItem.setErrorMessage(errorMessage.name());
        execTaskItemMapper.updateByPrimaryKeySelective(execTaskItem);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateTaskRunningStatus(String taskId) {
        ExecTask execTask = new ExecTask();
        execTask.setId(taskId);
        execTask.setStartTime(System.currentTimeMillis());
        execTask.setStatus(ExecStatus.RUNNING.name());
        execTaskMapper.updateByPrimaryKeySelective(execTask);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateTaskItemRunningStatus(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        // 更新任务项状态
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setId(taskItem.getId());
        execTaskItem.setStartTime(System.currentTimeMillis());
        execTaskItem.setStatus(ExecStatus.RUNNING.name());
        execTaskItem.setThreadId(request.getThreadId());
        execTaskItemMapper.updateByPrimaryKeySelective(execTaskItem);
    }

    public ExecTaskItem getRerunTaskItem(String id) {
        ExecTaskItemExample example = new ExecTaskItemExample();
        example.createCriteria().andTaskIdEqualTo(id).andRerunEqualTo(true);
        List<ExecTaskItem> execTaskItems = execTaskItemMapper.selectByExample(example);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(execTaskItems)) {
            throw new MSException("No test cases to rerun");
        }
        ExecTaskItem execTaskItem = execTaskItems.getFirst();
        return execTaskItem;
    }

    public AbstractMsTestElement getAbstractMsTestElement(byte[] msTestElementByte) {
        return getAbstractMsTestElement(new String(msTestElementByte));
    }

    public AbstractMsTestElement getAbstractMsTestElement(String msTestElementStr) {
        try {
            return ApiDataUtils.parseObject(msTestElementStr, AbstractMsTestElement.class);
            // 如果插件删除，会转换异常
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }

    /**
     * 复制文件到临时目录
     * @param fileIds
     * @param sourceDir
     * @return
     */
    public Map<String, String> copyFiles2TempDir(List<String> fileIds, String sourceDir) {
        Map<String, String> uploadFileMap = new HashMap<>();
        for (String fileId : fileIds) {
            String newFileId = IDGenerator.nextStr();
            String targetDir = DefaultRepositoryDir.getSystemTempDir();
            String fileName = apiFileResourceService.getFileNameByFileId(fileId, sourceDir);
            // 复制文件到临时目录
            apiFileResourceService.copyFile(sourceDir + "/" + fileId,
                    targetDir + "/" + newFileId,
                    fileName);
            uploadFileMap.put(fileId, newFileId);
        }
        return uploadFileMap;
    }
}
