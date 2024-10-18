package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.dto.converter.ApiScenarioImportParseResult;
import io.metersphere.api.dto.converter.ApiScenarioStepParseResult;
import io.metersphere.api.dto.export.MetersphereApiScenarioExportResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.parser.ApiScenarioImportParser;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
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

public class MetersphereParserApiScenario implements ApiScenarioImportParser {

    @Override
    public ApiScenarioImportParseResult parse(InputStream source, ApiScenarioImportRequest request) throws Exception {
        MetersphereApiScenarioExportResponse metersphereApiScenarioExportResponse = null;
        try {
            metersphereApiScenarioExportResponse = ApiDataUtils.parseObject(source, MetersphereApiScenarioExportResponse.class);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (metersphereApiScenarioExportResponse == null) {
            throw new MSException("解析失败，请确认是否是正确的文件");
        }
        return this.genApiScenarioPreImportAnalysisResult(metersphereApiScenarioExportResponse);
    }

    private ApiScenarioImportParseResult genApiScenarioPreImportAnalysisResult(MetersphereApiScenarioExportResponse metersphereApiScenarioExportResponse) {
        ApiScenarioImportParseResult returnResult = new ApiScenarioImportParseResult();

        returnResult.setRelatedApiDefinitions(metersphereApiScenarioExportResponse.getRelatedApiDefinitions());
        returnResult.setRelatedApiTestCaseList(metersphereApiScenarioExportResponse.getRelatedApiTestCaseList());
        returnResult.setApiScenarioCsvList(metersphereApiScenarioExportResponse.getApiScenarioCsvList());


        List<ApiScenarioDetail> exportScenarioList = metersphereApiScenarioExportResponse.getExportScenarioList();
        List<ApiScenarioDetail> relatedScenarioList = metersphereApiScenarioExportResponse.getRelatedScenarioList();
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap =
                metersphereApiScenarioExportResponse.getScenarioStepList().stream().collect(Collectors.groupingBy(ApiScenarioStepDTO::getScenarioId));
        Map<String, String> scenarioStepBlobMap = metersphereApiScenarioExportResponse.getScenarioStepBlobMap();

        for (ApiScenarioDetail apiScenarioDetail : exportScenarioList) {
            returnResult.getImportScenarioList().add(
                    this.parseApiScenarioStep(apiScenarioDetail, scenarioStepMap.getOrDefault(apiScenarioDetail.getId(), new ArrayList<>()), scenarioStepBlobMap));
        }

        for (ApiScenarioDetail apiScenarioDetail : relatedScenarioList) {
            returnResult.getRelatedScenarioList().add(
                    this.parseApiScenarioStep(apiScenarioDetail, scenarioStepMap.getOrDefault(apiScenarioDetail.getId(), new ArrayList<>()), scenarioStepBlobMap));
        }

        return returnResult;
    }

    private ApiScenarioImportDetail parseApiScenarioStep(ApiScenarioDetail apiScenarioDetail, List<ApiScenarioStepDTO> apiScenarioStepDTOList, Map<String, String> scenarioStepBlobMap) {
        ApiScenarioImportDetail apiScenarioImportDetail = new ApiScenarioImportDetail();
        BeanUtils.copyBean(apiScenarioImportDetail, apiScenarioDetail);
        ApiScenarioStepParseResult parseResult = this.parseStepDetails(apiScenarioStepDTOList, scenarioStepBlobMap);
        apiScenarioImportDetail.setSteps(parseResult.getStepList());
        apiScenarioImportDetail.setStepDetails(parseResult.getStepDetails());
        return apiScenarioImportDetail;
    }

    private ApiScenarioStepParseResult parseStepDetails(List<ApiScenarioStepDTO> apiScenarioStepDTOList, Map<String, String> scenarioStepBlobMap) {
        ApiScenarioStepParseResult apiScenarioStepParseResult = new ApiScenarioStepParseResult();
        Map<String, ApiScenarioStepRequest> stepRequestIdMap = new HashMap<>();

        int lastSize = 0;
        while (CollectionUtils.isNotEmpty(apiScenarioStepDTOList) && apiScenarioStepDTOList.size() != lastSize) {
            lastSize = apiScenarioStepDTOList.size();
            List<ApiScenarioStepDTO> notMatchedList = new ArrayList<>();
            for (ApiScenarioStepDTO stepDTO : apiScenarioStepDTOList) {
                String oldStepId = stepDTO.getId();
                if (!stepRequestIdMap.containsKey(stepDTO.getParentId()) && StringUtils.isNotBlank(stepDTO.getParentId())) {
                    notMatchedList.add(stepDTO);
                    continue;
                }
                ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
                BeanUtils.copyBean(stepRequest, stepDTO);
                // 赋值新ID防止和库内已有数据重复
                stepRequest.setId(IDGenerator.nextStr());

                // 使用旧ID用于配置Tree
                stepRequestIdMap.put(oldStepId, stepRequest);
                if (StringUtils.isBlank(stepDTO.getParentId())) {
                    apiScenarioStepParseResult.getStepList().add(stepRequest);
                    if (scenarioStepBlobMap.containsKey(oldStepId)) {
                        apiScenarioStepParseResult.getStepDetails().put(stepRequest.getId(), scenarioStepBlobMap.get(oldStepId).getBytes());
                    }
                } else if (stepRequestIdMap.containsKey(stepDTO.getParentId())) {
                    if (stepRequestIdMap.get(stepDTO.getParentId()).getChildren() == null) {
                        stepRequestIdMap.get(stepDTO.getParentId()).setChildren(new ArrayList<>());
                    }

                    stepRequestIdMap.get(stepDTO.getParentId()).getChildren().add(stepRequest);
                    if (scenarioStepBlobMap.containsKey(oldStepId)) {
                        apiScenarioStepParseResult.getStepDetails().put(stepRequest.getId(), scenarioStepBlobMap.get(oldStepId).getBytes());
                    }
                }
            }
            apiScenarioStepDTOList = notMatchedList;
        }
        return apiScenarioStepParseResult;
    }
}
