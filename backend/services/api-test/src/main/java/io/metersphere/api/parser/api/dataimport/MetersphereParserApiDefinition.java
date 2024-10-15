package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.export.MetersphereApiDefinitionExportResponse;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.parser.ApiDefinitionImportParser;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetersphereParserApiDefinition implements ApiDefinitionImportParser<ApiDefinitionImportFileParseResult> {

    @Override
    public ApiDefinitionImportFileParseResult parse(InputStream source, ImportRequest request) throws Exception {
        MetersphereApiDefinitionExportResponse metersphereApiExportResponse = null;
        try {
            metersphereApiExportResponse = ApiDataUtils.parseObject(source, MetersphereApiDefinitionExportResponse.class);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (metersphereApiExportResponse == null) {
            throw new MSException("解析失败，请确认选择的是 Metersphere 格式！");
        }
        return this.genApiDefinitionImport(metersphereApiExportResponse.getApiDefinitions());
    }

    @Override
    public ApiDefinitionImportDataAnalysisResult generateInsertAndUpdateData(ApiDefinitionImportFileParseResult importParser, List<ApiDefinitionDetail> existenceAllApiList) {
        Map<String, List<ApiDefinitionDetail>> existenceProtocolMap = new HashMap<>();

        ApiDefinitionImportDataAnalysisResult insertAndUpdateData = new ApiDefinitionImportDataAnalysisResult();

        for (ApiDefinitionDetail apiDefinitionDetail : existenceAllApiList) {
            String protocol = apiDefinitionDetail.getProtocol().toUpperCase();
            if (existenceProtocolMap.containsKey(protocol)) {
                existenceProtocolMap.get(protocol).add(apiDefinitionDetail);
            } else {
                existenceProtocolMap.put(protocol, new ArrayList<ApiDefinitionDetail>() {{
                    this.add(apiDefinitionDetail);
                }});
            }
        }

        Map<String, List<ApiDefinitionDetail>> importProtocolMap = new HashMap<>();
        for (ApiDefinitionDetail apiDefinitionDetail : importParser.getData()) {
            String protocol = apiDefinitionDetail.getProtocol().toUpperCase();
            if (importProtocolMap.containsKey(protocol)) {
                importProtocolMap.get(protocol).add(apiDefinitionDetail);
            } else {
                importProtocolMap.put(protocol, new ArrayList<ApiDefinitionDetail>() {{
                    this.add(apiDefinitionDetail);
                }});
            }
        }

        for (Map.Entry<String, List<ApiDefinitionDetail>> entry : importProtocolMap.entrySet()) {
            String protocol = entry.getKey();
            List<ApiDefinitionDetail> importDetail = entry.getValue();
            List<ApiDefinitionDetail> existenceApiDefinitionList = existenceProtocolMap.containsKey(protocol) ? existenceProtocolMap.get(protocol) : new ArrayList<>();

            if (StringUtils.equalsIgnoreCase(protocol, "HTTP")) {
                //HTTP类型，通过 Method & Path 组合判断，接口是否存在
                Map<String, ApiDefinitionDetail> savedApiDefinitionMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
                Map<String, ApiDefinitionDetail> importDataMap = importDetail.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));

                importDataMap.forEach((key, api) -> {
                    if (savedApiDefinitionMap.containsKey(key)) {
                        insertAndUpdateData.addExistenceApi(api, new ArrayList<>() {{
                            this.add(savedApiDefinitionMap.get(key));
                        }});
                    } else {
                        insertAndUpdateData.getInsertApiList().add(api);
                    }
                    this.addTestCaseAndMock(insertAndUpdateData, api.getId(), importParser.getCaseMap().get(api.getId()), importParser.getMockMap().get(api.getId()));
                });
            } else {
                //非HTTP类型，通过 name判断，后续处理会过滤掉路径不一致的
                Map<String, ApiDefinitionDetail> importDataMap = importDetail.stream().collect(Collectors.toMap(t -> t.getModulePath() + t.getName(), t -> t, (oldValue, newValue) -> newValue));
                Map<String, List<ApiDefinitionDetail>> savedApiDefinitionMap = existenceApiDefinitionList.stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getName));

                importDataMap.forEach((key, api) -> {
                    if (savedApiDefinitionMap.containsKey(key)) {
                        insertAndUpdateData.addExistenceApi(api, savedApiDefinitionMap.get(key));
                    } else {
                        insertAndUpdateData.getInsertApiList().add(api);
                    }
                    this.addTestCaseAndMock(insertAndUpdateData, api.getId(), importParser.getCaseMap().get(api.getId()), importParser.getMockMap().get(api.getId()));
                });
            }
        }
        return insertAndUpdateData;
    }

    private void addTestCaseAndMock(ApiDefinitionImportDataAnalysisResult insertAndUpdateData, String apiId, List<ApiTestCaseDTO> caseList, List<ApiDefinitionMockDTO> mockDTOList) {
        if (CollectionUtils.isNotEmpty(caseList)) {
            insertAndUpdateData.getApiIdAndTestCaseMap().put(apiId, caseList);
        }
        if (CollectionUtils.isNotEmpty(mockDTOList)) {
            insertAndUpdateData.getApiIdAndMockMap().put(apiId, mockDTOList);
        }
    }

    private ApiDefinitionImportFileParseResult genApiDefinitionImport(List<ApiDefinitionExportDetail> apiDefinitions) {
        List<ApiDefinitionExportDetail> distinctImportList = this.mergeApiCaseWithUniqueIdentification(apiDefinitions);
        ApiDefinitionImportFileParseResult returnDTO = new ApiDefinitionImportFileParseResult();
        distinctImportList.forEach(definitionImportDetail -> {
            String apiID = IDGenerator.nextStr();
            definitionImportDetail.setId(apiID);
            List<ApiTestCaseDTO> caseList = new ArrayList<>();
            List<ApiDefinitionMockDTO> mockList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(definitionImportDetail.getApiTestCaseList())) {
                definitionImportDetail.getApiTestCaseList().forEach(item -> {
                    item.setApiDefinitionId(apiID);
                    item.setProtocol(definitionImportDetail.getProtocol());
                    caseList.add(item);
                });
            }
            if (CollectionUtils.isNotEmpty(definitionImportDetail.getApiMockList())) {
                definitionImportDetail.getApiMockList().forEach(item -> {
                    item.setApiDefinitionId(apiID);
                    mockList.add(item);
                });
            }
            returnDTO.getData().add(definitionImportDetail);
            if (CollectionUtils.isNotEmpty(caseList)) {
                returnDTO.getCaseMap().put(apiID, ApiDefinitionImportUtils.apiCaseRename(caseList));
            }
            if (CollectionUtils.isNotEmpty(mockList)) {
                returnDTO.getMockMap().put(apiID, ApiDefinitionImportUtils.apiMockRename(mockList));
            }
        });

        return returnDTO;
    }

    //合并相同路径下的用例和mock
    private List<ApiDefinitionExportDetail> mergeApiCaseWithUniqueIdentification(List<ApiDefinitionExportDetail> apiDefinitions) {
        List<ApiDefinitionExportDetail> returnList = new ArrayList<>();
        Map<String, ApiDefinitionExportDetail> filterApiMap = new HashMap<>();
        Map<String, List<ApiTestCaseDTO>> uniqueCaseMap = new HashMap<>();
        Map<String, List<ApiDefinitionMockDTO>> uniqueMockMap = new HashMap<>();
        apiDefinitions.forEach((api) -> {
            String key = api.getProtocol() + StringUtils.SPACE + api.getModulePath() + StringUtils.SPACE + api.getName();
            if (StringUtils.equalsIgnoreCase(api.getProtocol(), "http")) {
                key = api.getMethod() + StringUtils.SPACE + api.getPath();
            }

            if (!filterApiMap.containsKey(key)) {
                filterApiMap.put(key, api);
            }
            if (CollectionUtils.isNotEmpty(api.getApiTestCaseList())) {
                if (uniqueCaseMap.containsKey(key)) {
                    uniqueCaseMap.get(key).addAll(api.getApiTestCaseList());
                } else {
                    uniqueCaseMap.put(key, api.getApiTestCaseList());
                }
            }
            if (CollectionUtils.isNotEmpty(api.getApiMockList())) {
                if (uniqueMockMap.containsKey(key)) {
                    uniqueMockMap.get(key).addAll(api.getApiMockList());
                } else {
                    uniqueMockMap.put(key, api.getApiMockList());
                }
            }

        });
        filterApiMap.forEach((key, api) -> {
            api.setApiTestCaseList(uniqueCaseMap.get(key));
            api.setApiMockList(uniqueMockMap.get(key));
            returnList.add(api);
        });
        return returnList;
    }

}
