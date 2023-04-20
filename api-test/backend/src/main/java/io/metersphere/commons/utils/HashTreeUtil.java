package io.metersphere.commons.utils;

import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.FakeError;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.dto.AttachmentBodyFile;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.BodyFile;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * 2021/7/28 3:37 下午
 */
public class HashTreeUtil {
    public static final String COMMON_CONFIG = "commonConfig";
    public static final String VARIABLES = "variables";
    public static final String VALUE = "value";
    public static final String ENABLE = "enable";
    public static final String NAME = "name";
    public static final String SCRIPT = ElementConstants.SCRIPT;

    public Map<String, Map<String, String>> getEnvParamsDataByHashTree(HashTree hashTree, BaseEnvironmentService apiTestEnvironmentService) {
        Map<String, Map<String, String>> returnMap = new HashMap<>();
        Map<String, List<String>> envParamMap = this.getEnvParamsMapByHashTree(hashTree);

        for (Map.Entry<String, List<String>> entry : envParamMap.entrySet()) {
            String envId = entry.getKey();
            List<String> params = entry.getValue();
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(envId);
            if (environment != null && environment.getConfig() != null) {
                try {
                    JSONObject configJson = JSONUtil.parseObject(environment.getConfig());
                    if (configJson.has(COMMON_CONFIG)) {
                        JSONObject commonConfig = configJson.optJSONObject(COMMON_CONFIG);
                        if (commonConfig.has(VARIABLES)) {
                            Map<String, String> envHeadMap = new HashMap<>();
                            JSONArray variablesArr = commonConfig.optJSONArray(VARIABLES);
                            for (int i = 0; i < variablesArr.length(); i++) {
                                JSONObject object = variablesArr.optJSONObject(i);
                                if (object.has(NAME) && object.has(VALUE)) {
                                    boolean isEnable = true;
                                    if (object.has(ENABLE)) {
                                        isEnable = object.getBoolean(ENABLE);
                                    }
                                    if (isEnable) {
                                        envHeadMap.put(object.optString(NAME), object.optString(VALUE));
                                    }
                                }
                            }
                            for (String param : params) {
                                String value = envHeadMap.get(param);
                                if (value == null) {
                                    value = StringUtils.EMPTY;
                                }
                                if (returnMap.containsKey(envId)) {
                                    returnMap.get(envId).put(param, value);
                                } else {
                                    Map<String, String> map = new HashMap<>();
                                    map.put(param, value);
                                    returnMap.put(envId, map);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return returnMap;
    }

    public Map<String, List<String>> getEnvParamsMapByHashTree(HashTree hashTree) {
        Map<String, List<String>> returnMap = new HashMap<>();
        if (hashTree != null) {
            for (Object hashTreeKey : hashTree.keySet()) {
                HashTree itemTree = hashTree.get(hashTreeKey);

                String scriptValue = StringUtils.EMPTY;
                try {
                    if (hashTreeKey instanceof JSR223PostProcessor) {
                        JSR223PostProcessor postProcessor = (JSR223PostProcessor) hashTreeKey;
                        scriptValue = postProcessor.getPropertyAsString(SCRIPT);
                    } else if (hashTreeKey instanceof JSR223PreProcessor) {
                        JSR223PreProcessor processor = (JSR223PreProcessor) hashTreeKey;
                        scriptValue = processor.getPropertyAsString(SCRIPT);
                    } else if (hashTreeKey instanceof JSR223Sampler) {
                        JSR223Sampler processor = (JSR223Sampler) hashTreeKey;
                        scriptValue = processor.getPropertyAsString(SCRIPT);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }

                if (StringUtils.isNotEmpty(scriptValue)) {
                    if (scriptValue.contains(RunningParamKeys.RUNNING_PARAMS_PREFIX)) {
                        String[] paramsArr = scriptValue.split(RunningParamKeys.RUNNING_PARAMS_PREFIX);
                        for (int i = 1; i < paramsArr.length; i++) {
                            String paramItem = paramsArr[i];
                            if (StringUtils.contains(paramItem, ".")) {
                                String envId = paramItem.split("\\.")[0];
                                String otherStr = paramItem.substring(envId.length() + 3);
                                String firstChar = otherStr.substring(0, 1);
                                String[] envParamsStr = otherStr.split(firstChar);
                                if (envParamsStr.length > 1) {
                                    String param = envParamsStr[1];
                                    if (returnMap.containsKey(envId)) {
                                        returnMap.get(envId).add(param);
                                    } else {
                                        List<String> list = new ArrayList<>();
                                        list.add(param);
                                        returnMap.put(envId, list);
                                    }
                                }
                            }
                        }
                    }
                }

                Map<String, List<String>> itemMap = this.getEnvParamsMapByHashTree(itemTree);

                for (Map.Entry<String, List<String>> entry : itemMap.entrySet()) {
                    String envId = entry.getKey();
                    List<String> params = entry.getValue();

                    if (returnMap.containsKey(envId)) {
                        for (String param : params) {
                            if (!returnMap.get(envId).contains(param)) {
                                returnMap.get(envId).add(param);
                            }
                        }
                    } else {
                        returnMap.put(envId, params);
                    }
                }
            }
        }
        return returnMap;
    }

    public Map<String, Map<String, String>> mergeParamDataMap(Map<String, Map<String, String>> execute_env_param_dataMap, Map<String, Map<String, String>> envParamsMap) {
        if (execute_env_param_dataMap == null) {
            execute_env_param_dataMap = new HashMap<>();
        }
        if (envParamsMap == null) {
            return execute_env_param_dataMap;
        }
        for (Map.Entry<String, Map<String, String>> paramEnvMapEntry : envParamsMap.entrySet()) {
            String envId = paramEnvMapEntry.getKey();
            Map<String, String> map = paramEnvMapEntry.getValue();
            if (execute_env_param_dataMap.containsKey(envId)) {
                execute_env_param_dataMap.get(envId).putAll(map);
            } else {
                execute_env_param_dataMap.put(envId, map);
            }
        }
        return execute_env_param_dataMap;
    }

    public static void addPositive(EnvironmentConfig envConfig, HashTree samplerHashTree, ParameterConfig config, String projectId, AbstractTestElement sample) {
        if (envConfig == null) {
            return;
        }
        if (!config.isOperating() && envConfig.isUseErrorCode()) {
            FakeError fakeError = new FakeError();
            fakeError.setHigherThanError(envConfig.isHigherThanError());
            fakeError.setProjectId(projectId);
            fakeError.setHigherThanSuccess(envConfig.isHigherThanSuccess());
            if (sample instanceof JDBCSampler) {
                sample.setProperty(SampleResult.MS_FAKE_ERROR, JSONUtil.toJSONString(fakeError));
            } else if (sample instanceof TCPSampler) {
                sample.setProperty(SampleResult.MS_FAKE_ERROR, JSONUtil.toJSONString(fakeError));
            }
        }
        if (CollectionUtils.isNotEmpty(envConfig.getAssertions())) {
            for (MsAssertions assertion : ElementUtil.copyAssertion(envConfig.getAssertions())) {
                assertion.toHashTree(samplerHashTree, assertion.getHashTree(), config);
            }
        }
    }

    public static void initRepositoryFiles(JmeterRunRequestDTO runRequest) {
        if (runRequest.getPool().isPool() || runRequest.getPool().isK8s()) {
            return;
        }
        List<AttachmentBodyFile> executeFileList = ApiFileUtil.getExecuteFile(runRequest.getHashTree(), runRequest.getReportId(), true);
        LoggerUtil.info("本次执行[" + runRequest.getReportId() + "]共需要[" + executeFileList.size() + "]个文件。");
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        if (fileMetadataService != null) {
            List<AttachmentBodyFile> downloadFileList = fileMetadataService.filterDownloadFileList(executeFileList);
            LoggerUtil.info("本次执行[" + runRequest.getReportId() + "]需要下载[" + downloadFileList.size() + "]个文件。开始下载。。。");
            fileMetadataService.downloadByAttachmentBodyFileList(downloadFileList);
        }
        LoggerUtil.info("本次执行[" + runRequest.getReportId() + "]需要下载[" + executeFileList.size() + "]个文件,下载结束。");
    }

    public static void downFile(
            List<BodyFile> files,
            Map<String, byte[]> multipartFiles,
            FileMetadataService fileMetadataService) {

        if (CollectionUtils.isNotEmpty(files)) {
            Map<String, String> repositoryFileMap = new HashMap<>();
            List<String> processFiles = new ArrayList<>();
            for (BodyFile bodyFile : files) {
                // 调试附件处理
                if (StringUtils.isNotBlank(bodyFile.getRefResourceId())) {
                    FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(bodyFile.getRefResourceId());
                    if (fileMetadata != null) {
                        bodyFile.setFileId(bodyFile.getRefResourceId());
                        bodyFile.setStorage(fileMetadata.getStorage());
                        processFiles.add(bodyFile.getFileId());
                        repositoryFileMap.put(bodyFile.getFileId(), bodyFile.getName());
                    }
                } else if (StringUtils.isNotBlank(bodyFile.getFileId())) {
                    repositoryFileMap.put(bodyFile.getFileId(), bodyFile.getName());
                } else {
                    File file = new File(bodyFile.getName());
                    if (file != null && file.exists()) {
                        byte[] fileByte = FileUtils.fileToByte(file);
                        if (fileByte != null) {
                            multipartFiles.put(file.getAbsolutePath(), fileByte);
                        }
                    }
                }
            }
            List<FileInfoDTO> fileList = fileMetadataService.downloadFileByIds(repositoryFileMap.keySet());
            if (CollectionUtils.isNotEmpty(fileList)) {
                // 处理返回文件
                fileList.forEach(repositoryFile -> {
                    if (ArrayUtils.isNotEmpty(repositoryFile.getFileByte())) {
                        String path = StringUtils.join(
                                FileUtils.BODY_FILE_DIR,
                                File.separator,
                                repositoryFileMap.get(repositoryFile.getId()));
                        // 调试文件路径
                        String key = processFiles.contains(repositoryFile.getId())
                                ? repositoryFileMap.get(repositoryFile.getId())
                                : path;
                        multipartFiles.put(key, repositoryFile.getFileByte());
                    }
                });
            }
        }
    }
}
