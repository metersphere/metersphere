package io.metersphere.api.parser.api.dataimport;

import io.metersphere.api.constants.ApiScenarioStatus;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.dto.converter.ApiScenarioImportParseResult;
import io.metersphere.api.dto.converter.ApiScenarioStepParseResult;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.scenario.ApiScenarioImportDetail;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.parser.ApiScenarioImportParser;
import io.metersphere.api.parser.api.har.HarUtils;
import io.metersphere.api.parser.api.har.model.Har;
import io.metersphere.api.parser.api.har.model.HarEntry;
import io.metersphere.api.parser.api.har.model.HarRequest;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.project.utils.DataBaseStringUtils;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HarParserApiScenario implements ApiScenarioImportParser {


    @Override
    public ApiScenarioImportParseResult parse(InputStream inputSource, ApiScenarioImportRequest request) throws Exception {
        Har har = null;
        try {
            har = HarUtils.read(inputSource);
        } catch (Exception e) {
            throw new MSException(e.getMessage());
        }

        if (ObjectUtils.isEmpty(har)) {
            throw new MSException("解析失败，请确认是否是正确的文件");
        }

        ApiScenarioImportParseResult scenarioImport = new ApiScenarioImportParseResult();
        scenarioImport.getImportScenarioList().add(this.parseImportFile(request.getProjectId(), har));
        return scenarioImport;
    }

    private ApiScenarioImportDetail parseImportFile(String projectId, Har har) {
        ApiScenarioImportDetail apiScenarioDetail = new ApiScenarioImportDetail();
        apiScenarioDetail.setName("Har Import");
        apiScenarioDetail.setPriority("P0");
        apiScenarioDetail.setStatus(ApiScenarioStatus.UNDERWAY.name());
        apiScenarioDetail.setGrouped(false);
        apiScenarioDetail.setDeleted(false);
        apiScenarioDetail.setLatest(true);
        apiScenarioDetail.setProjectId(projectId);

        ApiScenarioStepParseResult stepParseResult = this.parseScenarioStep(parseRequests(har), projectId);
        apiScenarioDetail.setSteps(stepParseResult.getStepList());
        apiScenarioDetail.setStepDetails(stepParseResult.getStepDetails());
        apiScenarioDetail.setStepTotal(CollectionUtils.size(apiScenarioDetail.getSteps()));

        return apiScenarioDetail;
    }

    private List<MsHTTPElement> parseRequests(Har har) {
        List<MsHTTPElement> resultList = new ArrayList<>();

        List<HarEntry> harEntryList = new ArrayList<>();
        if (har.log != null && har.log.entries != null) {
            harEntryList = har.log.entries;
        }
        for (HarEntry entry : harEntryList) {
            HarRequest harRequest = entry.request;
            if (harRequest != null) {
                // css 、 js 略过
                if (StringUtils.equalsIgnoreCase(harRequest.method, HttpMethodConstants.GET.name()) &&
                        StringUtils.endsWithAny(harRequest.url.toLowerCase(), ".svg", ".css", ".js", ".png", ".jpg", ".jpeg", ".gif")) {
                    continue;
                }
                String url = harRequest.url;
                if (url == null) {
                    continue;
                }
                //默认取路径的最后一块
                String[] nameArr = url.split("/");
                String reqName = nameArr[nameArr.length - 1];
                try {
                    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
                    if (url.contains("?")) {
                        url = url.split("\\?")[0];
                    }
                } catch (Exception e) {
                    LogUtils.error(e.getMessage(), e);
                }


                MsHTTPElement request = ApiDefinitionImportUtils.buildHttpRequest(reqName, url, harRequest.method);
                HarUtils.parseParameters(harRequest, request);
                HarUtils.parseRequestBody(harRequest, request.getBody());
                resultList.add(request);
            }
        }
        return resultList;
    }

    private ApiScenarioStepParseResult parseScenarioStep(List<MsHTTPElement> msElementList, String projectId) {
        ApiScenarioStepParseResult parseResult = new ApiScenarioStepParseResult();
        for (MsHTTPElement msTestElement : msElementList) {
            ApiScenarioStepRequest apiScenarioStep = new ApiScenarioStepRequest();
            apiScenarioStep.setId(IDGenerator.nextStr());
            apiScenarioStep.setProjectId(projectId);
            apiScenarioStep.setOriginProjectId(projectId);
            apiScenarioStep.setName(DataBaseStringUtils.parseMaxString(msTestElement.getName(), 255));
            apiScenarioStep.setUniqueId(IDGenerator.nextStr());
            msTestElement.setStepId(apiScenarioStep.getId());
            msTestElement.setProjectId(apiScenarioStep.getProjectId());
            apiScenarioStep.setConfig(new ProtocolConfig("HTTP", msTestElement.getMethod()));
            apiScenarioStep.setStepType(ApiScenarioStepType.CUSTOM_REQUEST.name());
            apiScenarioStep.setRefType(ApiScenarioStepRefType.DIRECT.name());
            msTestElement.setCustomizeRequest(true);
            byte[] stepBlobContent = JSON.toJSONString(msTestElement).getBytes();
            parseResult.getStepList().add(apiScenarioStep);
            if (stepBlobContent != null) {
                parseResult.getStepDetails().put(apiScenarioStep.getId(), stepBlobContent);
            }
        }
        return parseResult;
    }
}


