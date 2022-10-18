package io.metersphere.api.parse;


import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class ApiImportAbstractParser<T> implements ApiImportParser<T> {

    protected String projectId;

    protected String getApiTestStr(InputStream source) {
        StringBuilder testStr = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return testStr.toString();
    }

    protected String getBodyType(String contentType) {
        String bodyType = StringUtils.EMPTY;
        if (StringUtils.isBlank(contentType)) {
            return bodyType;
        }

        switch (contentType) {
            case "application/x-www-form-urlencoded":
                bodyType = Body.WWW_FROM;
                break;
            case "multipart/form-data":
                bodyType = Body.FORM_DATA;
                break;
            case "application/json":
                bodyType = Body.JSON_STR;
                break;
            case "application/xml":
                bodyType = Body.XML;
                break;
            case "application/octet-stream":
                bodyType = Body.BINARY;
                break;
            default:
                bodyType = Body.RAW;
        }
        return bodyType;
    }

    protected void addBodyHeader(MsHTTPSamplerProxy request) {
        String contentType = StringUtils.EMPTY;
        if (request.getBody() != null && StringUtils.isNotBlank(request.getBody().getType())) {
            switch (request.getBody().getType()) {
                case Body.WWW_FROM:
                    contentType = "application/x-www-form-urlencoded";
                    break;
                case Body.JSON_STR:
                    contentType = "application/json";
                    break;
                case Body.XML:
                    contentType = "application/xml";
                    break;
                case Body.BINARY:
                    contentType = "application/octet-stream";
                    break;
            }
            List<KeyValue> headers = request.getHeaders();
            if (headers == null) {
                headers = new ArrayList<>();
                request.setHeaders(headers);
            }
            if (StringUtils.isNotEmpty(contentType)) {
                addContentType(request.getHeaders(), contentType);
            }
        }
    }

    protected ApiDefinitionWithBLOBs buildApiDefinition(String id, String name, String path, String method, ApiTestImportRequest importRequest) {
        ApiDefinitionWithBLOBs apiDefinition = new ApiDefinitionWithBLOBs();
        apiDefinition.setName(name);
        apiDefinition.setPath(formatPath(path));
        apiDefinition.setProtocol(RequestTypeConstants.HTTP);
        apiDefinition.setMethod(method);
        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        if (StringUtils.equalsIgnoreCase("schedule", importRequest.getType())) {
            apiDefinition.setUserId(importRequest.getUserId());
            apiDefinition.setCreateUser(importRequest.getUserId());
        } else {
            apiDefinition.setUserId(SessionUtils.getUserId());
            apiDefinition.setCreateUser(SessionUtils.getUserId());
        }
        return apiDefinition;
    }

    public String formatPath(String url) {
        try {
            URL urlObject = new URL(url);
            String path = StringUtils.isBlank(urlObject.getPath()) ? "/" : urlObject.getPath();
            StringBuffer pathBuffer = new StringBuffer(path);
            if (StringUtils.isNotEmpty(urlObject.getQuery())) {
                pathBuffer.append("?").append(urlObject.getQuery());
            }
            return pathBuffer.toString();
        } catch (Exception ex) {
            return url;
        }
    }

    protected MsHTTPSamplerProxy buildRequest(String name, String path, String method) {
        MsHTTPSamplerProxy request = new MsHTTPSamplerProxy();
        request.setName(name);
        // 路径去掉域名/IP 地址，保留方法名称及参数
        request.setPath(formatPath(path));
        request.setMethod(method);
        request.setProtocol(RequestTypeConstants.HTTP);
        request.setId(UUID.randomUUID().toString());
        request.setHeaders(new ArrayList<>());
        request.setArguments(new ArrayList<>());
        request.setRest(new ArrayList<>());
        Body body = new Body();
        body.initKvs();
        body.initBinary();
        request.setBody(body);
        return request;
    }

    protected MsHTTPSamplerProxy buildRequest(String name, String path, String method, String jsonSchema) {
        MsHTTPSamplerProxy request = buildRequest(name, path, method);
        if (StringUtils.isNotBlank(jsonSchema)) {
            request.getBody().setJsonSchema(JSON.parseObject(jsonSchema));
            request.getBody().setFormat("JSON-SCHEMA");
        }
        return request;
    }

    protected void addContentType(List<KeyValue> headers, String contentType) {
        addHeader(headers, "Content-Type", contentType);
    }

    protected void addCookie(List<KeyValue> headers, String key, String value) {
        addCookie(headers, key, value, StringUtils.EMPTY, true);
    }

    protected void addCookie(List<KeyValue> headers, String key, String value, String description, boolean required) {
        boolean hasCookie = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase("Cookie", header.getName())) {
                hasCookie = true;
                String cookies = Optional.ofNullable(header.getValue()).orElse(StringUtils.EMPTY);
                header.setValue(cookies + key + "=" + value + ";");
            }
        }
        if (!hasCookie) {
            addHeader(headers, "Cookie", key + "=" + value + ";", description, StringUtils.EMPTY, required);
        }
    }

    protected void addHeader(List<KeyValue> headers, String key, String value) {
        addHeader(headers, key, value, StringUtils.EMPTY, StringUtils.EMPTY, true);
    }

    protected void addHeader(List<KeyValue> headers, String key, String value, String description, String contentType, boolean required) {
        boolean hasContentType = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase(header.getName(), key)) {
                hasContentType = true;
            }
        }
        if (!hasContentType) {
            headers.add(new KeyValue(key, value, description, contentType, required));
        }
    }

    protected ApiScenarioWithBLOBs parseScenario(MsScenario msScenario) {
        ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        scenarioWithBLOBs.setName(msScenario.getName());
        scenarioWithBLOBs.setProjectId(this.projectId);
        if (msScenario != null && CollectionUtils.isNotEmpty(msScenario.getHashTree())) {
            scenarioWithBLOBs.setStepTotal(msScenario.getHashTree().size());
        }
        scenarioWithBLOBs.setId(UUID.randomUUID().toString());
        scenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(msScenario));
        return scenarioWithBLOBs;
    }
}
