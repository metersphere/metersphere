package io.metersphere.api.parse.scenario;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.parse.api.har.HarUtils;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.parse.HarScenarioAbstractParser;
import io.metersphere.api.parse.api.har.model.*;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseResult;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang3.ObjectUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HarScenarioParser extends HarScenarioAbstractParser<ScenarioImport> {

    @Override
    public ScenarioImport parse(InputStream source, ApiTestImportRequest request) {
        Har har = null;
        try {
            har = HarUtils.read(source);
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        }

        if (ObjectUtils.isEmpty(har)) {
            MSException.throwException("解析失败，请确认选择的是 Har 格式！");
        }

        ScenarioImport scenarioImport = new ScenarioImport();

        String harName = request.getFileName();
        // 场景步骤
        LinkedList<MsTestElement> apiScenarioWithBLOBs = new LinkedList<>();

        MsScenario msScenario = new MsScenario();
        msScenario.setName(harName);
        this.projectId = request.getProjectId();
        if (!ObjectUtils.isEmpty(har.log) && !ObjectUtils.isEmpty(har.log.entries)) {
            parseItem(har.log.entries, msScenario, apiScenarioWithBLOBs);
        }

        // 生成场景对象
        List<ApiScenarioWithBLOBs> scenarioWithBLOBs = new LinkedList<>();
        parseScenarioWithBLOBs(scenarioWithBLOBs, msScenario);
        scenarioImport.setData(scenarioWithBLOBs);
        return scenarioImport;
    }

    private void parseScenarioWithBLOBs(List<ApiScenarioWithBLOBs> scenarioWithBLOBsList, MsScenario msScenario) {
        ApiScenarioWithBLOBs scenarioWithBLOBs = parseScenario(msScenario);
        scenarioWithBLOBsList.add(scenarioWithBLOBs);
    }

    private void parseItem(List<HarEntry> items, MsScenario scenario, LinkedList<MsTestElement> results) {
        for (HarEntry item : items) {
            MsHTTPSamplerProxy request = parseHar(item);
            if (request != null) {
                results.add(request);
            }
        }
        scenario.setHashTree(results);
    }

    private RequestResult getRequestResult(MsHTTPSamplerProxy samplerProxy, HarEntry harEntry) {
        HarRequest request = harEntry.request;
        HarResponse response = harEntry.response;

        RequestResult requestResult = new RequestResult();
        requestResult.setName("Response");
        requestResult.setUrl(request.url);
        requestResult.setMethod(request.method);
        if (samplerProxy.getBody() != null) {
            List<KeyValue> keyValueList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(request.queryString)) {
                for (HarQueryParam model : request.queryString) {
                    KeyValue keyValue = new KeyValue(model.name, model.value);
                    keyValueList.add(keyValue);
                }
            }
            if (!ObjectUtils.isEmpty(request.postData) && !ObjectUtils.isEmpty(request.postData.params)) {
                for (HarPostParam model : request.postData.params) {
                    KeyValue keyValue = new KeyValue(model.name, model.value);
                    keyValueList.add(keyValue);
                }
            }

            requestResult.setBody(JSON.toJSONString(keyValueList));
        }

        requestResult.setHeaders(JSON.toJSONString(request.headers));
        requestResult.setRequestSize(request.bodySize);
        if (!ObjectUtils.isEmpty(request.cookies)) {
            requestResult.setCookies(JSON.toJSONString(request.cookies));
        }

        ResponseResult responseResult = requestResult.getResponseResult();
        responseResult.setHeaders(JSON.toJSONString(response.headers));
        responseResult.setResponseCode(String.valueOf(response.status));
        responseResult.setResponseSize(response.bodySize);
        if (response.content != null && response.content.text != null) {
            responseResult.setBody(response.content.text);
            responseResult.setResponseMessage(response.content.text);
        }

        return requestResult;
    }
}
