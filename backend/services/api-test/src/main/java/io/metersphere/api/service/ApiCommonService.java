package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.ApiDefinitionExecuteInfo;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.ResponseBinaryBody;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private void setLinkFileInfo(String resourceId, List<ApiFile> apiFiles) {
        List<ApiFile> linkFiles = apiFiles.stream()
                .filter(file -> !file.getLocal() && !file.getDelete())
                .toList();
        List<String> linkFileIds = linkFiles.stream()
                .map(ApiFile::getFileId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(linkFileIds)) {
            return;
        }

        Map<String, String> fileNameMap = fileMetadataService.selectByList(linkFileIds)
                .stream()
                .collect(Collectors.toMap(FileMetadata::getId, FileMetadata::getName));

        for (ApiFile linkFile : linkFiles) {
            String fileName = fileNameMap.get(linkFile.getFileId());
            if (StringUtils.isBlank(fileName)) {
                // fileName 为空，则文件被删除，设置为已删除，并且设置文件名
                linkFile.setDelete(true);
                List<FileAssociation> fileAssociations = fileAssociationService.getByFileIdAndSourceId(resourceId, linkFile.getFileId());
                if (CollectionUtils.isNotEmpty(fileAssociations)) {
                    linkFile.setFileAlias(fileAssociations.get(0).getDeletedFileName());
                }
            } else {
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
     * @param msTestElement
     */
    public void setEnableCommonScriptProcessorInfo(AbstractMsTestElement msTestElement) {
        MsCommonElement msCommonElement = getMsCommonElement(msTestElement);
        Optional.ofNullable(msCommonElement).ifPresent(item -> setEnableCommonScriptProcessorInfo(List.of(item)));
    }

    /**
     * 设置使用脚本前后置的公共脚本信息
     *
     * @param commonElements
     */
    public void setEnableCommonScriptProcessorInfo(List<MsCommonElement> commonElements) {
        List<ScriptProcessor> scriptsProcessors = getEnableCommonScriptProcessors(commonElements);

        List<String> commonScriptIds = scriptsProcessors.stream()
                .map(processor -> processor.getCommonScriptInfo().getId())
                .toList();

        Map<String, CustomFunctionBlob> customFunctionBlobMap = customFunctionService.getBlobByIds(commonScriptIds).stream()
                .collect(Collectors.toMap(CustomFunctionBlob::getId, Function.identity()));

        Map<String, CustomFunction> customFunctionMap = customFunctionService.getByIds(commonScriptIds).stream()
                .collect(Collectors.toMap(CustomFunction::getId, Function.identity()));

        for (ScriptProcessor processor : scriptsProcessors) {
            CommonScriptInfo commonScriptInfo = processor.getCommonScriptInfo();
            CustomFunctionBlob customFunctionBlob = customFunctionBlobMap.get(commonScriptInfo.getId());
            CustomFunction customFunction = customFunctionMap.get(commonScriptInfo.getId());

            // 设置公共脚本信息
            Optional.ofNullable(customFunctionBlob.getParams()).ifPresent(paramsBlob -> {
                List<KeyValueParam> commonParams = JSON.parseArray(new String(paramsBlob), KeyValueParam.class);
                // 替换用户输入值
                commonParams.forEach(commonParam ->
                        Optional.ofNullable(commonScriptInfo.getParams()).ifPresent(params ->
                            params.stream()
                                    .filter(param -> StringUtils.equals(commonParam.getKey(), param.getKey()))
                                    .findFirst()
                                    .ifPresent(param -> commonParam.setValue(param.getValue()))
                        )
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
        List<ScriptProcessor> scriptsProcessors = processors.stream()
                .filter(processor -> processor instanceof ScriptProcessor)
                .map(processor -> (ScriptProcessor) processor)
                .filter(ScriptProcessor::getEnable)
                .filter(ScriptProcessor::isEnableCommonScript)
                .filter(ScriptProcessor::isValid)
                .collect(Collectors.toList());
        return scriptsProcessors;
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
     * 设置 MsHTTPElement 中的 method 等信息
     * @param httpElements
     * @param getDefinitionInfoFunc
     */
    public void setApiDefinitionExecuteInfo(List<MsHTTPElement> httpElements, Function<List<String>, List<ApiDefinitionExecuteInfo>> getDefinitionInfoFunc) {
        if (CollectionUtils.isNotEmpty(httpElements)) {
            List<String> resourceIds = httpElements.stream().map(MsHTTPElement::getResourceId).collect(Collectors.toList());
            // 获取接口模块信息
            Map<String, ApiDefinitionExecuteInfo> resourceModuleMap = getApiDefinitionExecuteInfoMap(getDefinitionInfoFunc, resourceIds);
            httpElements.forEach(httpElement -> {
                ApiDefinitionExecuteInfo definitionExecuteInfo = resourceModuleMap.get(httpElement.getResourceId());
                // httpElement 设置模块,请求方法等信息
                setApiDefinitionExecuteInfo(httpElement, definitionExecuteInfo);
            });
        }
    }


    /**
     * 获取资源 ID 和接口定义信息 的 Map
     * @param getDefinitionInfoFunc
     * @param resourceIds
     * @return
     */
    public Map<String, ApiDefinitionExecuteInfo> getApiDefinitionExecuteInfoMap(Function<List<String>, List<ApiDefinitionExecuteInfo>> getDefinitionInfoFunc, List<String> resourceIds) {
        Map<String, ApiDefinitionExecuteInfo> resourceModuleMap = getDefinitionInfoFunc.apply(resourceIds)
                .stream()
                .collect(Collectors.toMap(ApiDefinitionExecuteInfo::getResourceId, Function.identity()));
        return resourceModuleMap;
    }

    /**
     * 设置 MsHTTPElement 中的 method 等信息
     * @param msTestElement
     * @param definitionExecuteInfo
     */
    public void setApiDefinitionExecuteInfo(AbstractMsTestElement msTestElement, ApiDefinitionExecuteInfo definitionExecuteInfo) {
        if (msTestElement instanceof MsHTTPElement httpElement && definitionExecuteInfo != null) {
            httpElement.setModuleId(definitionExecuteInfo.getModuleId());
            httpElement.setMethod(definitionExecuteInfo.getMethod());
            httpElement.setPath(definitionExecuteInfo.getPath());
        }
    }

    /**
     * 给 httpElement 设置接口定义参数
     * @param apiDefinition
     * @param msTestElement
     */
    public void setApiDefinitionExecuteInfo(AbstractMsTestElement msTestElement, ApiDefinition apiDefinition) {
        setApiDefinitionExecuteInfo(msTestElement, BeanUtils.copyBean(new ApiDefinitionExecuteInfo(), apiDefinition));
    }
}
