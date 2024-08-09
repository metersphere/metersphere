package io.metersphere.api.parser.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiImportFileParseResult;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.parser.api.postman.PostmanCollection;
import io.metersphere.api.parser.api.postman.PostmanItem;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class PostmanParserApiDefinition extends PostmanAbstractParserParserApiDefinition<ApiImportFileParseResult> {

    @Override
    public ApiImportFileParseResult parse(InputStream source, ImportRequest request) throws JsonProcessingException {
        LogUtils.info("PostmanParser parse");
        String testStr = getApiTestStr(source);
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        if (postmanCollection == null || postmanCollection.getItem() == null || postmanCollection.getItem().isEmpty() || postmanCollection.getInfo() == null){
            throw new MSException("Postman collection is empty");
        }

        String modulePath = null;
        if (StringUtils.isNotBlank(postmanCollection.getInfo().getName())) {
            modulePath = "/" + postmanCollection.getInfo().getName();
        }
        LinkedHashMap<ApiDefinitionDetail, List<ApiDefinitionDetail>> allImportDetails = this.parseItem(postmanCollection.getItem(), modulePath, request);

        // 对于postman的导入： 本质就是将postman的数据导入为用例
        ApiImportFileParseResult apiImport = this.genApiDefinitionImport(allImportDetails);
        LogUtils.info("PostmanParser parse end");
        return apiImport;
    }

    protected LinkedHashMap<ApiDefinitionDetail, List<ApiDefinitionDetail>> parseItem(List<PostmanItem> items, String modulePath, ImportRequest request) {
        LinkedHashMap<ApiDefinitionDetail, List<ApiDefinitionDetail>> results = new LinkedHashMap<>();
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                String itemModulePath;
                if (StringUtils.isNotBlank(modulePath) && StringUtils.isNotBlank(item.getName())) {
                    itemModulePath = modulePath + "/" + item.getName();
                } else {
                    itemModulePath = item.getName();
                }
                results.putAll(parseItem(childItems, itemModulePath, request));
            } else {
                results.putAll(parsePostman(item, modulePath, request));
            }
        }
        return results;
    }

    private ApiImportFileParseResult genApiDefinitionImport(LinkedHashMap<ApiDefinitionDetail, List<ApiDefinitionDetail>> allImportDetails) {
        Map<ApiDefinitionDetail, List<ApiDefinitionDetail>> groupWithUniqueIdentification = this.mergeApiCaseWithUniqueIdentification(allImportDetails);
        ApiImportFileParseResult returnDTO = new ApiImportFileParseResult();
        groupWithUniqueIdentification.forEach((definitionImportDetail, caseData) -> {
            String apiID = IDGenerator.nextStr();
            definitionImportDetail.setId(apiID);
            List<ApiTestCaseDTO> caseList = new ArrayList<>();
            caseData.forEach(item -> {
                ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
                apiTestCaseDTO.setName(item.getName());
                apiTestCaseDTO.setPriority("P0");
                apiTestCaseDTO.setStatus(item.getStatus());
                apiTestCaseDTO.setProjectId(item.getProjectId());
                apiTestCaseDTO.setApiDefinitionId(definitionImportDetail.getId());
                apiTestCaseDTO.setFollow(false);
                apiTestCaseDTO.setMethod(item.getMethod());
                apiTestCaseDTO.setPath(item.getPath());
                apiTestCaseDTO.setRequest(item.getRequest());
                apiTestCaseDTO.setModulePath(item.getModulePath());
                apiTestCaseDTO.setModuleId(item.getModuleId());
                apiTestCaseDTO.setProtocol(item.getProtocol());
                apiTestCaseDTO.setProjectId(item.getProjectId());
                caseList.add(apiTestCaseDTO);
            });
            returnDTO.getData().add(definitionImportDetail);
            if (CollectionUtils.isNotEmpty(caseList)) {
                returnDTO.getCaseMap().put(apiID, caseList);
            }
        });

        return returnDTO;
    }

    private Map<ApiDefinitionDetail, List<ApiDefinitionDetail>> mergeApiCaseWithUniqueIdentification(LinkedHashMap<ApiDefinitionDetail, List<ApiDefinitionDetail>> allImportDetails) {
        Map<ApiDefinitionDetail, List<ApiDefinitionDetail>> returnMap = new HashMap<>();
        Map<String, ApiDefinitionDetail> filterApiMap = new HashMap<>();
        Map<String, List<ApiDefinitionDetail>> uniqueCaseMap = new HashMap<>();
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
            returnMap.put(api, this.apiCaseRename(uniqueCaseMap.get(key)));
        });
        return returnMap;
    }

    private List<ApiDefinitionDetail> apiCaseRename(List<ApiDefinitionDetail> caseList) {
        List<ApiDefinitionDetail> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionDetail apiCase : caseList) {
                String uniqueName = this.getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

}
