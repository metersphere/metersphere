package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.domain.ApiScenarioCsv;
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
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetersphereParserApiScenario implements ApiScenarioImportParser {

    @Override
    public ApiScenarioImportParseResult parse(InputStream source, ApiScenarioImportRequest request) throws Exception {
        MetersphereApiScenarioExportResponse metersphereApiScenarioExportResponse;
        try {
            metersphereApiScenarioExportResponse = ApiDataUtils.parseObject(source, MetersphereApiScenarioExportResponse.class);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (metersphereApiScenarioExportResponse == null) {
            throw new MSException("解析失败，请确认是否是正确的文件");
        }
        return this.genApiScenarioPreImportAnalysisResult(request.getProjectId(), metersphereApiScenarioExportResponse);
    }

    private ApiScenarioImportParseResult genApiScenarioPreImportAnalysisResult(String projectId, MetersphereApiScenarioExportResponse metersphereApiScenarioExportResponse) {
        ApiScenarioImportParseResult returnResult = new ApiScenarioImportParseResult();

        returnResult.setRelatedApiDefinitions(metersphereApiScenarioExportResponse.getRelatedApiDefinitions());
        returnResult.setRelatedApiTestCaseList(metersphereApiScenarioExportResponse.getRelatedApiTestCaseList());
        returnResult.setApiScenarioCsvList(metersphereApiScenarioExportResponse.getApiScenarioCsvList());

        List<ApiScenarioDetail> exportScenarioList = metersphereApiScenarioExportResponse.getExportScenarioList();
        List<ApiScenarioDetail> relatedScenarioList = metersphereApiScenarioExportResponse.getRelatedScenarioList();
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap =
                metersphereApiScenarioExportResponse.getScenarioStepList().stream().collect(Collectors.groupingBy(ApiScenarioStepDTO::getScenarioId));
        Map<String, String> scenarioStepBlobMap = metersphereApiScenarioExportResponse.getScenarioStepBlobMap();
        Map<String, List<ApiScenarioCsv>> apiScenarioCsvMap =
                metersphereApiScenarioExportResponse.getApiScenarioCsvList().stream().collect(Collectors.groupingBy(ApiScenarioCsv::getScenarioId));


        for (ApiScenarioDetail apiScenarioDetail : exportScenarioList) {
            returnResult.getImportScenarioList().add(
                    this.parseApiScenario(projectId, apiScenarioDetail, scenarioStepMap, scenarioStepBlobMap, apiScenarioCsvMap));
        }

        for (ApiScenarioDetail apiScenarioDetail : relatedScenarioList) {
            returnResult.getRelatedScenarioList().add(
                    this.parseApiScenario(projectId, apiScenarioDetail, scenarioStepMap, scenarioStepBlobMap, apiScenarioCsvMap));
        }

        return returnResult;
    }

    private ApiScenarioImportDetail parseApiScenario(String projectId,
                                                     ApiScenarioDetail apiScenarioDetail,
                                                     Map<String, List<ApiScenarioStepDTO>> apiScenarioStepMap,
                                                     Map<String, String> scenarioStepBlobMap,
                                                     Map<String, List<ApiScenarioCsv>> apiScenarioCsvMap) {
        ApiScenarioImportDetail apiScenarioImportDetail = new ApiScenarioImportDetail();
        BeanUtils.copyBean(apiScenarioImportDetail, apiScenarioDetail);
        ApiScenarioStepParseResult parseResult = this.parseStepDetails(apiScenarioStepMap, apiScenarioDetail.getId(), scenarioStepBlobMap);
        apiScenarioImportDetail.setSteps(parseResult.getStepList());
        apiScenarioImportDetail.setStepDetails(parseResult.getStepDetails());
        apiScenarioImportDetail.setProjectId(projectId);
        apiScenarioImportDetail.setApiScenarioCsvList(apiScenarioCsvMap.getOrDefault(apiScenarioDetail.getId(), new ArrayList<>()));
        return apiScenarioImportDetail;
    }

    private ApiScenarioStepParseResult parseStepDetails(Map<String, List<ApiScenarioStepDTO>> apiScenarioStepMap, String scenarioId, Map<String, String> scenarioStepBlobMap) {
        ApiScenarioStepParseResult apiScenarioStepParseResult = new ApiScenarioStepParseResult();

        List<ApiScenarioStepDTO> stepList = apiScenarioStepMap.getOrDefault(scenarioId, new ArrayList<>());
        for (ApiScenarioStepDTO stepDTO : stepList) {
            String oldStepId = stepDTO.getId();

            ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
            BeanUtils.copyBean(stepRequest, stepDTO);
            // 赋值新ID防止和库内已有数据重复
            stepRequest.setId(IDGenerator.nextStr());

            if (scenarioStepBlobMap.containsKey(oldStepId)) {
                apiScenarioStepParseResult.getStepDetails().put(stepRequest.getId(), scenarioStepBlobMap.get(oldStepId).getBytes());
            }
            stepRequest.setChildren(this.buildTreeStep(this.getChildScenarioStep(oldStepId, apiScenarioStepMap),
                    apiScenarioStepMap, scenarioStepBlobMap, apiScenarioStepParseResult));
            apiScenarioStepParseResult.getStepList().add(stepRequest);
        }

        return apiScenarioStepParseResult;
    }

    private List<ApiScenarioStepRequest> buildTreeStep(
            List<ApiScenarioStepDTO> childScenarioStep,
            Map<String, List<ApiScenarioStepDTO>> allScenarioStepMap,
            Map<String, String> scenarioStepBlobMap,
            ApiScenarioStepParseResult apiScenarioStepParseResult) {
        List<ApiScenarioStepRequest> returnList = new ArrayList<>();
        for (ApiScenarioStepDTO childDTO : childScenarioStep) {
            String oldChildId = childDTO.getId();
            ApiScenarioStepRequest childRequest = new ApiScenarioStepRequest();
            BeanUtils.copyBean(childRequest, childDTO);
            // 赋值新ID防止和库内已有数据重复
            childRequest.setId(IDGenerator.nextStr());
            // 使用旧ID用于配置Tree
            if (scenarioStepBlobMap.containsKey(oldChildId)) {
                apiScenarioStepParseResult.getStepDetails().put(childRequest.getId(), scenarioStepBlobMap.get(oldChildId).getBytes());
            }
            childRequest.setChildren(this.buildTreeStep(this.getChildScenarioStep(oldChildId, allScenarioStepMap),
                    allScenarioStepMap, scenarioStepBlobMap, apiScenarioStepParseResult));
            returnList.add(childRequest);
        }
        return returnList;
    }

    private List<ApiScenarioStepDTO> getChildScenarioStep(String parentId, Map<String, List<ApiScenarioStepDTO>> apiScenarioStepMap) {
        List<ApiScenarioStepDTO> childStepList = new ArrayList<>();
        apiScenarioStepMap.values().forEach(stepList -> {
            for (ApiScenarioStepDTO stepDTO : stepList) {
                if (StringUtils.equals(stepDTO.getParentId(), parentId)) {
                    childStepList.add(stepDTO);
                }
            }
        });
        return childStepList;
    }
}
