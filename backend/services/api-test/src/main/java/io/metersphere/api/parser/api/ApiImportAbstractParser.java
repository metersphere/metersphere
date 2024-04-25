package io.metersphere.api.parser.api;


import io.metersphere.api.dto.converter.ApiDefinitionImportDetail;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.parser.ImportParser;
import io.metersphere.project.dto.environment.auth.NoAuth;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public abstract class ApiImportAbstractParser<T> implements ImportParser<T> {

    protected String projectId;

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



    protected ApiDefinitionImportDetail buildApiDefinition(String name, String path, String method,String modulePath, ImportRequest importRequest) {
        ApiDefinitionImportDetail apiDefinition = new ApiDefinitionImportDetail();
        apiDefinition.setName(name);
        apiDefinition.setPath(formatPath(path));
        apiDefinition.setProtocol(importRequest.getProtocol());
        apiDefinition.setMethod(method);
        apiDefinition.setProjectId(this.projectId);
        apiDefinition.setCreateUser(importRequest.getUserId());
        apiDefinition.setModulePath(modulePath);
        apiDefinition.setResponse(new ArrayList<>());
        return apiDefinition;
    }

    protected MsHTTPElement buildHttpRequest(String name, String path, String method) {
        MsHTTPElement request = new MsHTTPElement();
        request.setName(name);
        // 路径去掉域名/IP 地址，保留方法名称及参数
        request.setPath(formatPath(path));
        request.setMethod(method);
        request.setHeaders(new ArrayList<>());
        request.setQuery(new ArrayList<>());
        request.setRest(new ArrayList<>());
        request.setBody(new Body());
        MsHTTPConfig httpConfig = new MsHTTPConfig();
        httpConfig.setConnectTimeout(60000L);
        httpConfig.setResponseTimeout(60000L);
        request.setOtherConfig(httpConfig);
        request.setAuthConfig(new NoAuth());
        Body body = new Body();
        body.setBinaryBody(new BinaryBody());
        body.setFormDataBody(new FormDataBody());
        body.setXmlBody(new XmlBody());
        body.setRawBody(new RawBody());
        body.setNoneBody(new NoneBody());
        body.setJsonBody(new JsonBody());
        body.setWwwFormBody(new WWWFormBody());
        body.setNoneBody(new NoneBody());
        body.setBodyType(Body.BodyType.NONE.name());
        request.setBody(body);
        return request;
    }

    protected String formatPath(String url) {
        try {
            URI urlObject = new URI(url);
            return StringUtils.isBlank(urlObject.getPath()) ? url : urlObject.getPath();
        } catch (Exception ex) {
            //只需要返回？前的路径
            return url.split("\\?")[0];
        }
    }
}
