package io.metersphere.api.parser.api;


import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import io.metersphere.api.dto.converter.ApiImportFileParseResult;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.export.MetersphereApiExportResponse;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.utils.ApiDataUtils;
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

public class MetersphereParserApiDefinition extends HttpApiDefinitionImportAbstractParser<ApiImportFileParseResult> {

    @Override
    public ApiImportFileParseResult parse(InputStream source, ImportRequest request) throws Exception {
        MetersphereApiExportResponse metersphereApiExportResponse = null;
        try {
            metersphereApiExportResponse = ApiDataUtils.parseObject(source, MetersphereApiExportResponse.class);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (metersphereApiExportResponse == null) {
            throw new MSException("解析失败，请确认选择的是 Metersphere 格式！");
        }
        return this.genApiDefinitionImport(metersphereApiExportResponse.getApiDefinitions());
    }

    private ApiImportFileParseResult genApiDefinitionImport(List<ApiDefinitionExportDetail> apiDefinitions) {
        List<ApiDefinitionExportDetail> distinctImportList = this.mergeApiCaseWithUniqueIdentification(apiDefinitions);
        ApiImportFileParseResult returnDTO = new ApiImportFileParseResult();
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
                returnDTO.getCaseMap().put(apiID, this.apiCaseRename(caseList));
            }
            if (CollectionUtils.isNotEmpty(mockList)) {
                returnDTO.getMockMap().put(apiID, this.apiMockRename(mockList));
            }
        });

        return returnDTO;
    }

    private List<ApiTestCaseDTO> apiCaseRename(List<ApiTestCaseDTO> caseList) {
        List<ApiTestCaseDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiTestCaseDTO apiCase : caseList) {
                String uniqueName = this.getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

    private List<ApiDefinitionMockDTO> apiMockRename(List<ApiDefinitionMockDTO> caseList) {
        List<ApiDefinitionMockDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionMockDTO apiMock : caseList) {
                String uniqueName = this.getUniqueName(apiMock.getName(), caseNameList);
                apiMock.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiMock);
            }
        }
        return returnList;
    }

    //合并相同路径下的用例和mock
    private List<ApiDefinitionExportDetail> mergeApiCaseWithUniqueIdentification(List<ApiDefinitionExportDetail> apiDefinitions) {
        List<ApiDefinitionExportDetail> returnList = new ArrayList<>();
        Map<String, ApiDefinitionExportDetail> filterApiMap = new HashMap<>();
        Map<String, List<ApiTestCaseDTO>> uniqueCaseMap = new HashMap<>();
        Map<String, List<ApiDefinitionMockDTO>> uniqueMockMap = new HashMap<>();
        apiDefinitions.forEach((api) -> {
            String key = api.getMethod() + StringUtils.SPACE + api.getPath();
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
