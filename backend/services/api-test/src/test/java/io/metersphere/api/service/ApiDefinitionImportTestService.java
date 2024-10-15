package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import io.metersphere.api.dto.export.MetersphereApiDefinitionExportResponse;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.service.definition.ApiDefinitionImportService;
import io.metersphere.api.utils.ApiDataUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ApiDefinitionImportTestService extends ApiDefinitionImportService {
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

    public void compareApiBlobList(List<ApiDefinitionBlob> apiDefinitionList, List<ApiDefinitionBlob> newApiDefinitionList, int apiChangedCount) {
        if (apiChangedCount == 0) {
            Assertions.assertEquals(apiDefinitionList.size(), newApiDefinitionList.size());
        }
        Map<String, ApiDefinitionBlob> oldApiBlobMap = apiDefinitionList.stream().collect(Collectors.toMap(ApiDefinitionBlob::getId, Function.identity()));
        Map<String, ApiDefinitionBlob> newApiBlobMap = newApiDefinitionList.stream().collect(Collectors.toMap(ApiDefinitionBlob::getId, Function.identity()));

        int diffApiCount = 0;
        for (Map.Entry<String, ApiDefinitionBlob> entry : oldApiBlobMap.entrySet()) {
            ApiDefinitionBlob oldBlob = entry.getValue();
            ApiDefinitionBlob newBlob = newApiBlobMap.get(entry.getKey());

            boolean dataIsSame = dataIsSame(ApiDataUtils.parseObject(new String(oldBlob.getRequest()), MsHTTPElement.class), ApiDataUtils.parseObject(new String(newBlob.getRequest()), MsHTTPElement.class));
            if (!dataIsSame) {
                diffApiCount++;
            }
        }
        Assertions.assertEquals(apiChangedCount, diffApiCount);
    }

    public void compareApiTestCaseList(List<ApiTestCase> apiTestCaseList, List<ApiTestCase> newApiTestCaseList, int apiTestCaseChangeCount, int apiTestCaseAddCount) {
        Assertions.assertEquals(apiTestCaseList.size() + apiTestCaseAddCount, newApiTestCaseList.size());
        Map<String, ApiTestCase> oldDataMap = apiTestCaseList.stream().collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));
        Map<String, ApiTestCase> newDataMap = newApiTestCaseList.stream().collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));

        int diffCaseCount = 0;
        for (Map.Entry<String, ApiTestCase> entry : oldDataMap.entrySet()) {
            ApiTestCase oldCase = entry.getValue();
            ApiTestCase newCase = newDataMap.get(entry.getKey());
            if (!Objects.equals(oldCase.getUpdateTime(), newCase.getUpdateTime())) {
                diffCaseCount++;
            }
        }
        Assertions.assertEquals(apiTestCaseChangeCount, diffCaseCount);
    }

    public List<ApiDefinitionBlob> selectBlobByProjectId(String projectId) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andProjectIdEqualTo(projectId);
        List<String> apiIdList = apiDefinitionMapper.selectByExample(apiDefinitionExample).stream().map(ApiDefinition::getId).toList();

        if (CollectionUtils.isEmpty(apiIdList)) {
            return new ArrayList<>();
        } else {
            ApiDefinitionBlobExample example = new ApiDefinitionBlobExample();
            example.createCriteria().andIdIn(apiIdList);
            return apiDefinitionBlobMapper.selectByExampleWithBLOBs(example);
        }
    }

    public List<ApiDefinition> selectApiDefinitionByProjectId(String projectId) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andProjectIdEqualTo(projectId);
        return apiDefinitionMapper.selectByExample(apiDefinitionExample);
    }

    public void checkApiModuleChange(List<ApiDefinition> oldApiDefinition, List<ApiDefinition> newApiDefinition, int moduleChangeCount) {
        Map<String, ApiDefinition> oldDataMap = oldApiDefinition.stream().collect(Collectors.toMap(ApiDefinition::getId, Function.identity()));
        Map<String, ApiDefinition> newDataMap = newApiDefinition.stream().collect(Collectors.toMap(ApiDefinition::getId, Function.identity()));

        int diffApiCount = 0;
        for (Map.Entry<String, ApiDefinition> entry : oldDataMap.entrySet()) {
            ApiDefinition oldData = entry.getValue();
            ApiDefinition newData = newDataMap.get(entry.getKey());
            if (!StringUtils.equals(oldData.getModuleId(), newData.getModuleId())) {
                diffApiCount++;
            }
        }
        Assertions.assertEquals(moduleChangeCount, diffApiCount);
    }

    public void compareApiExport(MetersphereApiDefinitionExportResponse exportResponse, List<ApiDefinitionBlob> exportApiBlobs) {
        Assertions.assertEquals(exportResponse.getApiDefinitions().size(), exportApiBlobs.size());
        List<ApiDefinitionExportDetail> compareList = new ArrayList<>();
        for (ApiDefinitionBlob blob : exportApiBlobs) {
            for (ApiDefinitionExportDetail exportDetail : exportResponse.getApiDefinitions()) {
                boolean dataIsSame = dataIsSame(ApiDataUtils.parseObject(new String(blob.getRequest()), MsHTTPElement.class), (MsHTTPElement) exportDetail.getRequest());
                if (dataIsSame) {
                    compareList.add(exportDetail);
                    break;
                }
            }
        }
        Assertions.assertEquals(exportResponse.getApiDefinitions().size(), compareList.size());
    }
}