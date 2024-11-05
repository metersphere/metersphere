package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.parser.ApiDefinitionImportParser;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class HttpApiDefinitionImportAbstractParser<T> implements ApiDefinitionImportParser<T> {

    @Override
    public ApiDefinitionImportDataAnalysisResult generateInsertAndUpdateData(ApiDefinitionImportFileParseResult importParser, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        //        API类型，通过 Method & Path 组合判断，接口是否存在
        Map<String, ApiDefinitionDetail> savedApiDefinitionMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
        Map<String, ApiDefinitionDetail> importDataMap = importParser.getData().stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));

        ApiDefinitionImportDataAnalysisResult insertAndUpdateData = new ApiDefinitionImportDataAnalysisResult();

        importDataMap.forEach((key, api) -> {
            if (savedApiDefinitionMap.containsKey(key)) {
                insertAndUpdateData.addExistenceApi(api, new ArrayList<>() {{
                    this.add(savedApiDefinitionMap.get(key));
                }});
            } else {
                insertAndUpdateData.getInsertApiList().add(api);
            }
            List<ApiTestCaseDTO> caseList = importParser.getCaseMap().get(api.getId());
            if (CollectionUtils.isNotEmpty(caseList)) {
                insertAndUpdateData.getApiIdAndTestCaseMap().put(api.getId(), caseList);
            }
            List<ApiDefinitionMockDTO> mockDTOList = importParser.getMockMap().get(api.getId());
            if (CollectionUtils.isNotEmpty(mockDTOList)) {
                insertAndUpdateData.getApiIdAndMockMap().put(api.getId(), mockDTOList);
            }
        });

        return insertAndUpdateData;
    }

    protected String getApiTestStr(InputStream source) {
        StringBuilder testStr = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
            source.close();
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        return StringUtils.isNotBlank(testStr) ? testStr.toString() : StringUtils.EMPTY;
    }


    protected ApiDefinitionDetail buildApiDefinition(String name, String path, String method, String modulePath, ImportRequest importRequest) {
        ApiDefinitionDetail apiDefinition = new ApiDefinitionDetail();
        apiDefinition.setId(IDGenerator.nextStr());
        if (name != null) {
            apiDefinition.setName(StringUtils.trim(name));
            if (apiDefinition.getName().length() > 255) {
                apiDefinition.setName(apiDefinition.getName().substring(0, 250) + "...");
            }
        }
        apiDefinition.setPath(ApiDefinitionImportUtils.formatPath(StringUtils.trim(path)));
        apiDefinition.setProtocol(StringUtils.trim(importRequest.getProtocol()));
        apiDefinition.setMethod(StringUtils.trim(method));
        apiDefinition.setProjectId(StringUtils.trim(importRequest.getProjectId()));
        apiDefinition.setModulePath(StringUtils.trim(modulePath));
        apiDefinition.setResponse(new ArrayList<>());
        return apiDefinition;
    }

    protected MsHTTPElement buildHttpRequest(String name, String path, String method) {
        return ApiDefinitionImportUtils.buildHttpRequest(name, path, method);
    }
}
