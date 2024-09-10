package io.metersphere.api.parser.api.dataimport;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.parser.ApiDefinitionImportParser;
import io.metersphere.api.parser.jmeter.xstream.MsSaveService;
import io.metersphere.api.parser.ms.MsTestElementParser;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class JmeterParserApiDefinition implements ApiDefinitionImportParser<ApiDefinitionImportFileParseResult> {


    @Override
    public ApiDefinitionImportFileParseResult parse(InputStream inputSource, ImportRequest request) throws Exception {
        try {
            Object scriptWrapper = MsSaveService.loadElement(inputSource);
            HashTree hashTree = this.getHashTree(scriptWrapper);
            MsTestElementParser parser = new MsTestElementParser();
            AbstractMsTestElement msTestElement = parser.parse(hashTree);
            List<AbstractMsProtocolTestElement> msElement = parser.getAbstractMsProtocolTestElement(msTestElement);
            LinkedHashMap<ApiDefinitionDetail, List<ApiTestCaseDTO>> allImportDetails = this.parseImportFile(request.getProjectId(), msElement);
            String moduleName = StringUtils.trim(parser.parseTestPlanName(hashTree));
            return this.genApiDefinitionImport(allImportDetails, moduleName);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("当前JMX版本不兼容");
        }
    }

    private ApiDefinitionImportFileParseResult genApiDefinitionImport(LinkedHashMap<ApiDefinitionDetail, List<ApiTestCaseDTO>> allImportDetails, String moduleName) {
        Map<ApiDefinitionDetail, List<ApiTestCaseDTO>> groupWithUniqueIdentification = this.mergeApiCaseWithUniqueIdentification(allImportDetails);
        ApiDefinitionImportFileParseResult returnDTO = new ApiDefinitionImportFileParseResult();
        groupWithUniqueIdentification.forEach((definitionImportDetail, caseData) -> {
            String apiID = IDGenerator.nextStr();
            definitionImportDetail.setId(apiID);
            definitionImportDetail.setModulePath(moduleName);
            returnDTO.getData().add(definitionImportDetail);
            caseData.forEach(item -> {
                item.setId(IDGenerator.nextStr());
                item.setApiDefinitionId(apiID);
            });
            if (CollectionUtils.isNotEmpty(caseData)) {
                returnDTO.getCaseMap().put(apiID, caseData);
            }
        });

        return returnDTO;
    }

    private Map<ApiDefinitionDetail, List<ApiTestCaseDTO>> mergeApiCaseWithUniqueIdentification(LinkedHashMap<ApiDefinitionDetail, List<ApiTestCaseDTO>> allImportDetails) {
        Map<ApiDefinitionDetail, List<ApiTestCaseDTO>> returnMap = new HashMap<>();
        Map<String, ApiDefinitionDetail> filterApiMap = new HashMap<>();
        Map<String, List<ApiTestCaseDTO>> uniqueCaseMap = new HashMap<>();
        allImportDetails.forEach((api, apiCase) -> {
            String key = api.getMethod() + StringUtils.SPACE + api.getPath();
            if (!filterApiMap.containsKey(key)) {
                filterApiMap.put(key, api);
            }
            if (uniqueCaseMap.containsKey(key)) {
                uniqueCaseMap.get(key).addAll(apiCase);
            } else {
                uniqueCaseMap.put(key, apiCase);
            }
        });
        filterApiMap.forEach((key, api) -> {
            returnMap.put(api, ApiDefinitionImportUtils.apiCaseRename(uniqueCaseMap.get(key)));
        });
        return returnMap;
    }

    private LinkedHashMap<ApiDefinitionDetail, List<ApiTestCaseDTO>> parseImportFile(String projectId, List<AbstractMsProtocolTestElement> msElement) {
        LinkedHashMap<ApiDefinitionDetail, List<ApiTestCaseDTO>> returnMap = new LinkedHashMap<>();

        ApiPluginService apiPluginService = CommonBeanFactory.getBean(ApiPluginService.class);
        assert apiPluginService != null;

        List<ProtocolDTO> protocolDTOList = apiPluginService.getProtocolsByProjectId(projectId);

        Map<String, String> polymorphicNameMap = protocolDTOList.stream().collect(Collectors.toMap(ProtocolDTO::getPolymorphicName, ProtocolDTO::getProtocol));

        for (AbstractMsProtocolTestElement protocolTestElement : msElement) {
            ApiDefinitionDetail definition = new ApiDefinitionDetail();
            definition.setName(StringUtils.trim(protocolTestElement.getName()));
            if (protocolTestElement instanceof MsHTTPElement msHTTPElement) {
                definition.setMethod(StringUtils.trim(msHTTPElement.getMethod()));
                definition.setPath(StringUtils.trim(msHTTPElement.getPath()));
                definition.setProtocol(ApiConstants.HTTP_PROTOCOL);
            } else {
                definition.setProtocol(polymorphicNameMap.get(protocolTestElement.getClass().getSimpleName()));
                definition.setMethod(definition.getProtocol());
            }
            if (StringUtils.isBlank(definition.getProtocol())) {
                continue;
            }
            definition.setRequest(protocolTestElement);
            definition.setResponse(new ArrayList<>());


            ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
            apiTestCaseDTO.setName(definition.getName());
            apiTestCaseDTO.setPriority("P0");
            apiTestCaseDTO.setStatus(definition.getStatus());
            apiTestCaseDTO.setProjectId(definition.getProjectId());
            apiTestCaseDTO.setFollow(false);
            apiTestCaseDTO.setMethod(definition.getMethod());
            apiTestCaseDTO.setPath(definition.getPath());
            apiTestCaseDTO.setRequest(definition.getRequest());
            apiTestCaseDTO.setProtocol(definition.getProtocol());
            apiTestCaseDTO.setProjectId(definition.getProjectId());

            returnMap.put(definition, new ArrayList<>(Collections.singletonList(apiTestCaseDTO)));
        }
        return returnMap;
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    @Override
    public ApiDefinitionImportDataAnalysisResult generateInsertAndUpdateData(ApiDefinitionImportFileParseResult importParser, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        List<ApiDefinitionDetail> importDataList = importParser.getData();
        Map<String, List<ApiDefinitionDetail>> protocolImportMap = importDataList.stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProtocol));
        Map<String, List<ApiDefinitionDetail>> existenceProtocolMap = existenceApiDefinitionList.stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProtocol));

        ApiDefinitionImportDataAnalysisResult insertAndUpdateData = new ApiDefinitionImportDataAnalysisResult();
        for (Map.Entry<String, List<ApiDefinitionDetail>> entry : protocolImportMap.entrySet()) {
            List<ApiDefinitionDetail> importList = entry.getValue();
            List<ApiDefinitionDetail> existenceList = existenceProtocolMap.get(entry.getKey());

            ApiDefinitionImportDataAnalysisResult httpResult = compareApiData(importList, existenceList, entry.getKey());
            insertAndUpdateData.getInsertApiList().addAll(httpResult.getInsertApiList());
            insertAndUpdateData.getExistenceApiList().addAll(httpResult.getExistenceApiList());
        }
        insertAndUpdateData.getApiIdAndTestCaseMap().putAll(importParser.getCaseMap());
        return insertAndUpdateData;
    }

    private ApiDefinitionImportDataAnalysisResult compareApiData(List<ApiDefinitionDetail> importData, List<ApiDefinitionDetail> existenceApiData, String protocol) {
        ApiDefinitionImportDataAnalysisResult insertAndUpdateData = new ApiDefinitionImportDataAnalysisResult();

        if (CollectionUtils.isEmpty(importData)) {
            return insertAndUpdateData;
        }

        if (CollectionUtils.isEmpty(existenceApiData)) {
            insertAndUpdateData.setInsertApiList(importData);
            return insertAndUpdateData;
        }

        //        API类型，通过 Method & Path 组合判断，接口是否存在
        Map<String, ApiDefinitionDetail> savedApiDefinitionMap = null;
        Map<String, ApiDefinitionDetail> importDataMap = null;

        if (StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL)) {
            savedApiDefinitionMap = existenceApiData.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
            importDataMap = importData.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
        } else {
            savedApiDefinitionMap = existenceApiData.stream().collect(Collectors.toMap(ApiDefinition::getName, t -> t, (oldValue, newValue) -> newValue));
            importDataMap = importData.stream().collect(Collectors.toMap(ApiDefinition::getName, t -> t, (oldValue, newValue) -> newValue));
        }

        for (Map.Entry<String, ApiDefinitionDetail> entry : importDataMap.entrySet()) {
            if (savedApiDefinitionMap.containsKey(entry.getKey())) {
                List<ApiDefinitionDetail> existenceList = new ArrayList<>();
                existenceList.add(savedApiDefinitionMap.get(entry.getKey()));
                insertAndUpdateData.addExistenceApi(entry.getValue(), existenceList);
            } else {
                insertAndUpdateData.getInsertApiList().add(entry.getValue());
            }
        }
        return insertAndUpdateData;
    }
}
