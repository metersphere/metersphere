package io.metersphere.commons.utils;

import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.assertions.MsAssertionRegex;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.BodyFile;
import io.metersphere.service.ExtErrorReportLibraryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

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
    public static final String SCRIPT = "script";

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
                            JSONArray variablesArr = commonConfig.getJSONArray(VARIABLES);
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
                                    value = "";
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

    public synchronized Map<String, List<String>> getEnvParamsMapByHashTree(HashTree hashTree) {
        Map<String, List<String>> returnMap = new HashMap<>();
        if (hashTree != null) {
            for (Object hashTreeKey : hashTree.keySet()) {
                HashTree itemTree = hashTree.get(hashTreeKey);

                String scriptValue = "";
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
                } catch (Exception ignored) {
                    LogUtil.error(ignored);
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

    public MsAssertions duplicateRegexInAssertions(List<MsAssertions> compareList, MsAssertions target) {
        if (target != null && CollectionUtils.isNotEmpty(target.getRegex())) {
            List<MsAssertionRegex> compareRegexList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(compareList)) {
                for (MsAssertions assertions : compareList) {
                    if (assertions != null && CollectionUtils.isNotEmpty(assertions.getRegex())) {
                        compareRegexList.addAll(assertions.getRegex());
                    }
                }
            }

            List<MsAssertionRegex> duplicatedList = new ArrayList<>();
            for (MsAssertionRegex regex : target.getRegex()) {
                boolean isExit = false;
                for (MsAssertionRegex compareRegex : compareRegexList) {
                    if (StringUtils.equals(regex.getType(), compareRegex.getType())
                            && StringUtils.equals(regex.getSubject(), compareRegex.getSubject())
                            && StringUtils.equals(regex.getExpression(), compareRegex.getExpression())) {
                        isExit = true;
                        break;
                    }
                }
                if (!isExit) {
                    duplicatedList.add(regex);
                }
            }
            target.setRegex(duplicatedList);
        }
        return target;
    }

    public static List<MsAssertions> getErrorReportByProjectId(String projectId, boolean higherThanSuccess, boolean higherThanError) {
        ExtErrorReportLibraryService service = CommonBeanFactory.getBean(ExtErrorReportLibraryService.class);
        return service.getAssertionByProjectIdAndStatusIsOpen(projectId, higherThanSuccess, higherThanError);
    }

    public static void addPositive(EnvironmentConfig envConfig, HashTree samplerHashTree, ParameterConfig config, String projectId) {
        if (envConfig == null) {
            return;
        }
        if (!config.isOperating() && envConfig.isUseErrorCode()) {
            List<MsAssertions> errorReportAssertion = HashTreeUtil.getErrorReportByProjectId(projectId, envConfig.isHigherThanSuccess(), envConfig.isHigherThanError());
            for (MsAssertions assertion : errorReportAssertion) {
                assertion.toHashTree(samplerHashTree, assertion.getHashTree(), config);
            }
        }
        if (CollectionUtils.isNotEmpty(envConfig.getAssertions())) {
            for (MsAssertions assertion : envConfig.getAssertions()) {
                assertion.toHashTree(samplerHashTree, assertion.getHashTree(), config);
            }
        }
    }

    public static void initRepositoryFiles(JmeterRunRequestDTO runRequest) {
        if (runRequest.getPool().isPool() || runRequest.getPool().isK8s()) {
            return;
        }
        List<BodyFile> files = new LinkedList<>();
        FileUtils.getExecuteFiles(runRequest.getHashTree(), runRequest.getReportId(), files);
        if (CollectionUtils.isNotEmpty(files)) {
            Map<String, String> repositoryFileMap = new HashMap<>();
            for (BodyFile bodyFile : files) {
                if (!StringUtils.equals(bodyFile.getStorage(), StorageConstants.LOCAL.name())
                        && StringUtils.isNotBlank(bodyFile.getFileId())) {
                    repositoryFileMap.put(bodyFile.getFileId(), bodyFile.getName());
                }
            }
            FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
            if (fileMetadataService != null) {
                Map<String, byte[]> multipartFiles = new LinkedHashMap<>();
                List<FileInfoDTO> fileInfoDTOList = fileMetadataService.downloadFileByIds(repositoryFileMap.keySet());
                fileInfoDTOList.forEach(repositoryFile -> {
                    if (repositoryFile.getFileByte() != null) {
                        multipartFiles.put(FileUtils.BODY_FILE_DIR + File.separator + repositoryFileMap.get(repositoryFile.getId()), repositoryFile.getFileByte());
                    }
                });
                for (Map.Entry<String, byte[]> fileEntry : multipartFiles.entrySet()) {
                    String fileName = fileEntry.getKey();
                    byte[] fileBytes = fileEntry.getValue();
                    FileUtils.createFile(fileName, fileBytes);
                }
            }
        }
    }
}
