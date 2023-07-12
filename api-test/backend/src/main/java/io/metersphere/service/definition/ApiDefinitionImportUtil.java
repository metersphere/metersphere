package io.metersphere.service.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.request.ApiSyncCaseRequest;
import io.metersphere.xpack.api.service.ApiDefinitionSyncService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ApiDefinitionImportUtil {
    public static final String HEADERS = "headers";
    public static final String ARGUMENTS = "arguments";
    public static final String REST = "rest";
    public static final String BODY = "body";
    public static final String JSONSCHEMA = "jsonSchema";
    public static final String PROPERTIES = "properties";


    public static Boolean checkIsSynchronize(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ApiSyncCaseRequest apiSyncCaseRequest = null;
        Boolean toUpdate = false;
        ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
        if (apiDefinitionSyncService != null) {
            toUpdate = apiDefinitionSyncService.getProjectApplications(existApi.getProjectId());
            apiSyncCaseRequest = apiDefinitionSyncService.getApiSyncCaseRequest(existApi.getProjectId());
        }
        //Compare the basic information of the API. If it contains the comparison that needs to be done for the synchronization information,
        // put the data into the to-be-synchronized
        if (apiSyncCaseRequest == null) {
            return false;
        }
        Boolean diffBasicInfo = delBasicInfo(existApi, apiDefinition, apiSyncCaseRequest, toUpdate);
        if (diffBasicInfo != null) return diffBasicInfo;

        Boolean diffApiRequest = delRequest(existApi, apiDefinition, objectMapper, apiSyncCaseRequest, toUpdate);
        if (diffApiRequest != null) return diffApiRequest;

        Boolean diffResponse = delResponse(existApi, apiDefinition, objectMapper);
        if (diffResponse != null) return diffResponse;
        return false;
    }

    /**
     * 比较导入的与系统中重复的两个api的基础信息
     *
     * @param existApi
     * @param apiDefinition
     * @param apiSyncCaseRequest
     * @param toUpdate
     * @return
     */
    private static Boolean delBasicInfo(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ApiSyncCaseRequest apiSyncCaseRequest, Boolean toUpdate) {
        if (!StringUtils.equals(apiDefinition.getMethod(), existApi.getMethod())) {
            if (apiSyncCaseRequest.getMethod() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }
        if (!StringUtils.equals(apiDefinition.getProtocol(), existApi.getProtocol())) {
            if (apiSyncCaseRequest.getProtocol() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getPath(), existApi.getPath())) {
            if (apiSyncCaseRequest.getPath() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }
        if (!StringUtils.equals(apiDefinition.getCreateUser(), existApi.getCreateUser())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getStatus(), existApi.getStatus()) && StringUtils.isNotBlank(existApi.getStatus()) && StringUtils.isNotBlank(apiDefinition.getStatus())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getTags(), existApi.getTags())) {
            if (apiDefinition.getTags() != null && Objects.equals(apiDefinition.getTags(), StringUtils.EMPTY) && existApi.getTags() != null && Objects.equals(existApi.getTags(), StringUtils.EMPTY)) {
                return true;
            }
        }

        if (!StringUtils.equals(existApi.getRemark(), apiDefinition.getRemark()) && StringUtils.isNotBlank(existApi.getRemark()) && StringUtils.isNotBlank(apiDefinition.getRemark())) {
            return true;
        }

        if (!StringUtils.equals(existApi.getDescription(), apiDefinition.getDescription()) && StringUtils.isNotBlank(existApi.getDescription()) && StringUtils.isNotBlank(apiDefinition.getDescription())) {
            return true;
        }
        return null;
    }

    /**
     * 比较导入的与系统中重复的两个api的响应体信息
     *
     * @param existApi
     * @param apiDefinition
     * @param objectMapper
     * @return
     */
    private static Boolean delRequest(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper, ApiSyncCaseRequest apiSyncCaseRequest, Boolean toUpdate) {
        JsonNode exApiRequest = null;
        JsonNode apiRequest = null;
        try {
            exApiRequest = objectMapper.readTree(existApi.getRequest());
            apiRequest = objectMapper.readTree(apiDefinition.getRequest());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiRequest == null || apiRequest == null) {
            return exApiRequest != null || apiRequest != null;
        }

        List<String> compareProList = Arrays.asList(HEADERS, ARGUMENTS, REST);

        Map<String, Boolean> applicationMap = new HashMap<>(4);
        applicationMap.put(HEADERS, apiSyncCaseRequest.getHeaders());
        applicationMap.put(ARGUMENTS, apiSyncCaseRequest.getQuery());
        applicationMap.put(REST, apiSyncCaseRequest.getRest());
        applicationMap.put(BODY, apiSyncCaseRequest.getBody());

        boolean diffByNodes = false;
        for (String property : compareProList) {
            JsonNode exApiJsonNode = exApiRequest.get(property);
            JsonNode apiJsonNode = apiRequest.get(property);
            if (exApiJsonNode != null && apiJsonNode != null) {
                diffByNodes = getDiffByArrayNodes(apiRequest, exApiRequest, objectMapper, property);
                if (diffByNodes) {
                    if (toUpdate && applicationMap.get(property)) {
                        apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                    }
                    break;
                }
            }
        }
        if (diffByNodes) {
            return true;
        }
        return delBody(apiDefinition, objectMapper, toUpdate, exApiRequest, apiRequest, applicationMap);
    }

    private static Boolean delResponse(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper) {
        JsonNode exApiResponse = null;
        JsonNode apiResponse = null;

        if (StringUtils.isBlank(apiDefinition.getResponse()) || StringUtils.isBlank(existApi.getResponse())) {
            return !StringUtils.isBlank(apiDefinition.getResponse()) || !StringUtils.isBlank(existApi.getResponse());
        }

        try {
            exApiResponse = objectMapper.readTree(existApi.getResponse());
            apiResponse = objectMapper.readTree(apiDefinition.getResponse());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiResponse == null || apiResponse == null) {
            return exApiResponse != null || apiResponse != null;
        }

        if (exApiResponse.get(HEADERS) != null && apiResponse.get(HEADERS) != null) {
            if (!StringUtils.equals(exApiResponse.get(HEADERS).toString(), apiResponse.get(HEADERS).toString())) {
                return true;
            }
        }

        if (exApiResponse.get(PropertyConstant.TYPE) != null && apiResponse.get(PropertyConstant.TYPE) != null) {
            if (!StringUtils.equals(exApiResponse.get(PropertyConstant.TYPE).toString(), apiResponse.get(PropertyConstant.TYPE).toString())) {
                return true;
            }
        }

        if (exApiResponse.get("name") != null && apiResponse.get("name") != null) {
            if (!StringUtils.equals(exApiResponse.get("name").toString(), apiResponse.get("name").toString())) {
                return true;
            }
        }

        if (exApiResponse.get(BODY) != null && apiResponse.get(BODY) != null) {
            if (!StringUtils.equals(exApiResponse.get(BODY).toString(), apiResponse.get(BODY).toString())) {
                return true;
            }
        }

        if (exApiResponse.get("statusCode") != null && apiResponse.get("statusCode") != null) {
            if (!StringUtils.equals(exApiResponse.get("statusCode").toString(), apiResponse.get("statusCode").toString())) {
                return true;
            }
        }

        if (exApiResponse.get("enable") != null && apiResponse.get("enable") != null) {
            return !StringUtils.equals(exApiResponse.get("enable").toString(), apiResponse.get("enable").toString());
        }
        return null;
    }

    private static boolean getDiffByArrayNodes(JsonNode apiRequest, JsonNode exApiRequest, ObjectMapper objectMapper, String name) {
        JsonNode apiNameNode = apiRequest.get(name);
        JsonNode caseNameNode = exApiRequest.get(name);
        if (apiNameNode == null || caseNameNode == null) {
            return false;
        }
        Map<String, String> apiMap = new HashMap<>();
        getKeyNameMap(apiNameNode, objectMapper, apiMap, "name");
        Map<String, String> exApiMap = new HashMap<>();
        getKeyNameMap(caseNameNode, objectMapper, exApiMap, "name");
        if (apiMap.size() != exApiMap.size()) {
            return true;
        }
        Set<String> apiKey = apiMap.keySet();
        Set<String> exApiKey = exApiMap.keySet();
        List<String> collect = apiKey.stream().filter(exApiKey::contains).collect(Collectors.toList());
        if (collect.size() != apiKey.size()) {
            return true;
        }
        return false;
    }

    public static void getKeyNameMap(JsonNode apiNameNode, ObjectMapper objectMapper, Map<String, String> nameMap, String nodeKey) {
        for (int i = 0; i < apiNameNode.size(); i++) {
            JsonNode apiName = apiNameNode.get(i);
            if (apiName.has(nodeKey)) {
                String keyName = null;
                try {
                    keyName = objectMapper.writeValueAsString(apiName.get(nodeKey));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if (StringUtils.isNotBlank(keyName) && !StringUtils.equals(keyName, "\"\"") && !StringUtils.equals(keyName, "null")) {
                    try {
                        nameMap.put(apiName.get(nodeKey).toString(), objectMapper.writeValueAsString(apiName));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static Boolean delBody(ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper, Boolean toUpdate, JsonNode exApiRequest, JsonNode apiRequest, Map<String, Boolean> applicationMap) {
        JsonNode exBodyNode = exApiRequest.get(BODY);
        JsonNode bodyNode = apiRequest.get(BODY);
        if (exBodyNode != null && bodyNode != null) {
            JsonNode exRowNode = exBodyNode.get("raw");
            JsonNode rowNode = bodyNode.get("raw");
            if (exRowNode != null && rowNode != null) {
                if (!StringUtils.equals(exRowNode.asText(), rowNode.asText())) {
                    if (applicationMap.get(BODY)) {
                        apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                    }
                    return true;
                }
            }

            boolean diffByNodes = getDiffByArrayNodes(bodyNode, exBodyNode, objectMapper, "kvs");
            if (diffByNodes && toUpdate && applicationMap.get(BODY)) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            if (diffByNodes) {
                return true;
            }
            JsonNode exApiJsonSchema = exBodyNode.get(JSONSCHEMA);
            JsonNode apiJsonSchema = bodyNode.get(JSONSCHEMA);
            if (exApiJsonSchema == null || apiJsonSchema == null) {
                return false;
            }

            JsonNode exApiProperties = exApiJsonSchema.get(PROPERTIES);
            JsonNode apiProperties = apiJsonSchema.get(PROPERTIES);
            if (exApiProperties == null || apiProperties == null) {
                return false;
            }
            boolean diffJsonschema = replenishCaseProperties(exApiProperties, apiProperties);
            if (diffJsonschema && toUpdate && applicationMap.get(BODY)) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return diffJsonschema;
        }
        return null;
    }


    private static boolean replenishCaseProperties(JsonNode exApiProperties, JsonNode apiProperties) {

        Iterator<Map.Entry<String, JsonNode>> apiFields = apiProperties.fields();
        Iterator<Map.Entry<String, JsonNode>> exApiFields = exApiProperties.fields();
        boolean diffProp = false;
        while (apiFields.hasNext()) {
            Map.Entry<String, JsonNode> apiNode = apiFields.next();
            if (diffProp) {
                break;
            }
            if (exApiFields.hasNext()) {
                Map.Entry<String, JsonNode> exChildNode = null;
                Map.Entry<String, JsonNode> exNode = exApiFields.next();
                if (StringUtils.equalsIgnoreCase(apiNode.getKey(), exNode.getKey())) {
                    exChildNode = exNode;
                } else {
                    diffProp = true;
                }
                if (exChildNode == null) {
                    continue;
                }
                JsonNode value = apiNode.getValue();
                JsonNode value1 = exChildNode.getValue();
                JsonNode apiPropertiesNode = value.get(PROPERTIES);
                JsonNode exApiPropertiesNode = value1.get(PROPERTIES);
                if (apiPropertiesNode == null || exApiPropertiesNode == null) {
                    continue;
                }
                replenishCaseProperties(exApiPropertiesNode, apiPropertiesNode);
            } else {
                return true;
            }
        }
        return false;
    }

    public static ApiTestCaseWithBLOBs addNewCase(ApiDefinitionWithBLOBs apiDefinition) {
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setProjectId(apiDefinition.getProjectId());
        apiTestCase.setName(apiDefinition.getName());
        apiTestCase.setRequest(apiDefinition.getRequest());
        return apiTestCase;
    }

    public static void setModule(ApiDefinitionWithBLOBs item, ApiModuleMapper apiModuleMapper) {
        if (item != null && StringUtils.isEmpty(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(item.getProjectId()).andProtocolEqualTo(item.getProtocol()).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName());
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                item.setModuleId(modules.get(0).getId());
                item.setModulePath(modules.get(0).getName());
            }
        }
    }

    public static void sendImportApiNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, String context, String event, String tip) {
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        BeanMap beanMap = new BeanMap(apiDefinitionWithBLOBs);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiDefinitionWithBLOBs.getId()).subject(Translator.get(tip)).paramMap(paramMap).excludeSelf(true).event(event).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public static void sendImportCaseNotice(ApiTestCase apiTestCase, String context, String event, String tip) {
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        BeanMap beanMap = new BeanMap(apiTestCase);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiTestCase.getId()).subject(Translator.get(tip)).paramMap(paramMap).excludeSelf(true).event(event).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public static ApiDefinitionResult getApiDefinitionResult(ApiDefinitionWithBLOBs apiDefinition, boolean isUpdate) {
        ApiDefinitionResult apiDefinitionResult = new ApiDefinitionResult();
        BeanUtils.copyBean(apiDefinitionResult, apiDefinition);
        apiDefinitionResult.setUpdated(isUpdate);
        return apiDefinitionResult;
    }

    public static Map<String, List<ApiTestCaseWithBLOBs>> getOldCaseMap(List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs, ApiTestCaseMapper apiTestCaseMapper) {
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap;
        List<String> definitionIds = repeatApiDefinitionWithBLOBs.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdIn(definitionIds).andStatusNotEqualTo(ApiTestDataStatus.TRASH.getValue());
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(testCaseExample);
        oldCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
        return oldCaseMap;
    }

}
