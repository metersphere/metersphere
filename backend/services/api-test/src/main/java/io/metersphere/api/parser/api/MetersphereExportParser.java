package io.metersphere.api.parser.api;

import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.export.ApiDefinitionExportResponse;
import io.metersphere.api.dto.export.MetersphereApiDefinitionExportResponse;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetersphereExportParser {

    public ApiDefinitionExportResponse parse(List<ApiDefinitionWithBlob> apiDefinitionList, List<ApiTestCaseWithBlob> apiTestCaseList, List<ApiMockWithBlob> apiMockList, Map<String, String> moduleMap) {

        Map<String, List<ApiTestCaseWithBlob>> apiTestCaseMap = apiTestCaseList.stream().collect(Collectors.groupingBy(ApiTestCaseWithBlob::getApiDefinitionId));
        Map<String, List<ApiMockWithBlob>> apiMockMap = apiMockList.stream().collect(Collectors.groupingBy(ApiMockWithBlob::getApiDefinitionId));

        MetersphereApiDefinitionExportResponse response = new MetersphereApiDefinitionExportResponse();
        for (ApiDefinitionWithBlob blob : apiDefinitionList) {
            ApiDefinitionExportDetail detail = new ApiDefinitionExportDetail();
            if (blob.getRequest() != null) {
                detail.setRequest(ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class));
            }
            if (blob.getResponse() != null) {
                detail.setResponse(ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class));
            }
            detail.setName(blob.getName());
            detail.setProtocol(blob.getProtocol());
            detail.setMethod(blob.getMethod());
            detail.setPath(blob.getPath());
            detail.setStatus(blob.getStatus());
            detail.setTags(blob.getTags());
            detail.setPos(blob.getPos());
            detail.setDescription(blob.getDescription());

            String moduleName;
            if (StringUtils.equals(blob.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
                moduleName = Translator.get("api_unplanned_request");
            } else {
                moduleName = moduleMap.get(blob.getModuleId());
            }
            detail.setModulePath(moduleName);
            response.getApiDefinitions().add(detail);

            if (apiTestCaseMap.containsKey(blob.getId())) {
                for (ApiTestCaseWithBlob apiTestCaseWithBlob : apiTestCaseMap.get(blob.getId())) {
                    ApiTestCaseDTO dto = new ApiTestCaseDTO();
                    dto.setName(apiTestCaseWithBlob.getName());
                    dto.setPriority(apiTestCaseWithBlob.getPriority());
                    dto.setStatus(apiTestCaseWithBlob.getStatus());
                    dto.setTags(apiTestCaseWithBlob.getTags());
                    dto.setRequest(ApiDataUtils.parseObject(new String(apiTestCaseWithBlob.getRequest()), AbstractMsTestElement.class));
                    detail.getApiTestCaseList().add(dto);
                }
            }
            if (apiMockMap.containsKey(blob.getId())) {
                for (ApiMockWithBlob apiMockWithBlob : apiMockMap.get(blob.getId())) {
                    ApiDefinitionMockDTO mockDTO = new ApiDefinitionMockDTO();
                    mockDTO.setName(apiMockWithBlob.getName());
                    mockDTO.setTags(apiMockWithBlob.getTags());
                    mockDTO.setEnable(apiMockWithBlob.getEnable());
                    mockDTO.setStatusCode(apiMockWithBlob.getStatusCode());
                    mockDTO.setMockMatchRule(ApiDataUtils.parseObject(new String(apiMockWithBlob.getMatching()), MockMatchRule.class));
                    mockDTO.setResponse(ApiDataUtils.parseObject(new String(apiMockWithBlob.getResponse()), MockResponse.class));
                    detail.getApiMockList().add(mockDTO);
                }
            }
        }
        return response;
    }
}
