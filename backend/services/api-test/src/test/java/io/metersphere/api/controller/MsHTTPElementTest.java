package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.request.assertion.*;
import io.metersphere.api.dto.request.assertion.body.*;
import io.metersphere.api.dto.request.http.*;
import io.metersphere.api.dto.request.http.auth.BasicAuth;
import io.metersphere.api.dto.request.http.auth.DigestAuth;
import io.metersphere.api.dto.request.http.auth.HTTPAuth;
import io.metersphere.api.dto.request.http.auth.NoAuth;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.request.processors.*;
import io.metersphere.api.dto.request.processors.extract.JSONPathExtract;
import io.metersphere.api.dto.request.processors.extract.RegexExtract;
import io.metersphere.api.dto.request.processors.extract.XPathExtract;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:17
 */
public class MsHTTPElementTest {
    public MsHTTPElementTest() {
        ApiDataUtils.setResolver(MsHTTPElement.class);
    }

    @Test
    public void bodyTest() {
        MsHTTPElement msHTTPElement = getMsHttpElement();
        msHTTPElement.setBody(getGeneralBody());
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
    }

    public static Body getGeneralBody() {
        Body body = new Body();
        body.setBodyType(Body.BodyType.FORM_DATA.name());

        FormDataBody formDataBody = new FormDataBody();
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setEnable(false);
        formDataKV.setContentType("text/plain");
        formDataKV.setEncode(true);
        formDataKV.setMaxLength(10);
        formDataKV.setMinLength(8);
        formDataKV.setParamType("text");
        formDataKV.setDescription("test");
        formDataKV.setRequired(true);
        formDataKV.setValue("@email");
        formDataKV.setKey("key");
        FormDataKV formDataFileKV = new FormDataKV();
        BodyFile bodyFile = new BodyFile();
        bodyFile.setFileId("aaa");
        bodyFile.setFileName("aaa");
        formDataFileKV.setFiles(List.of(bodyFile));
        formDataFileKV.setKey("fileKey");
        formDataBody.setFromValues(List.of(formDataKV));
        body.setFormDataBody(formDataBody);

        WWWFormBody wwwFormBody = new WWWFormBody();
        wwwFormBody.setFromValues(List.of(formDataKV));
        body.setWwwFormBody(wwwFormBody);

        JsonBody jsonBody = new JsonBody();
        jsonBody.setJsonSchema("{}");
        jsonBody.setEnableJsonSchema(false);
        body.setJsonBody(jsonBody);

        body.setNoneBody(new NoneBody());

        RawBody rawBody = new RawBody();
        rawBody.setValue("A");
        body.setRawBody(rawBody);

        XmlBody xmlBody = new XmlBody();
        xmlBody.setValue("<a/>");
        body.setXmlBody(xmlBody);

        BinaryBody binaryBody = new BinaryBody();
        binaryBody.setBodyFile(bodyFile);
        body.setBinaryBody(binaryBody);

        return body;
    }

    @Test
    public void authConfigTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();

        List authConfigs = new ArrayList<>();

        authConfigs.add(new NoAuth());

        BasicAuth basicAuth = new BasicAuth();
        basicAuth.setUserName("test");
        basicAuth.setPassword("passwd");
        authConfigs.add(basicAuth);

        DigestAuth digestAuth = new DigestAuth();
        digestAuth.setUserName("test");
        digestAuth.setPassword("passwd");
        authConfigs.add(digestAuth);

        for (Object authConfig : authConfigs) {
            msHTTPElement.setAuthConfig((HTTPAuth) authConfig);
            String json = ApiDataUtils.toJSONString(msHTTPElement);
            Assertions.assertNotNull(json);
            Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
        }
    }

    @Test
    public void msProcessorTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();

        List processors = new ArrayList<>();

        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setEnable(true);
        scriptProcessor.setScript("script");
        scriptProcessor.setScriptLanguage("js");
        scriptProcessor.setJsrEnable(true);
        processors.add(scriptProcessor);

        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setScript("script");
        sqlProcessor.setEnable(true);
        sqlProcessor.setDataSourceId("dataSourceId");
        KeyValueEnableParam keyValueParam = new KeyValueEnableParam();
        keyValueParam.setKey("key");
        keyValueParam.setValue("value");
        sqlProcessor.setVariables(List.of(keyValueParam));
        sqlProcessor.setResultVariable("ddd");
        sqlProcessor.setQueryTimeout(1111);
        sqlProcessor.setVariableNames("test");
        processors.add(sqlProcessor);

        TimeWaitingProcessor timeWaitingProcessor = new TimeWaitingProcessor();
        timeWaitingProcessor.setDelay(1000);
        timeWaitingProcessor.setEnable(true);
        processors.add(timeWaitingProcessor);

        CommonScriptProcessor commonScriptProcessor = new CommonScriptProcessor();
        commonScriptProcessor.setEnable(true);
        commonScriptProcessor.setScriptId("11111");
        KeyValueParam commonScriptParam = new KeyValueParam();
        commonScriptParam.setKey("11");
        commonScriptParam.setValue("11");
        commonScriptProcessor.setParams(List.of(commonScriptParam));
        processors.add(commonScriptProcessor);

        ExtractPostProcessor extractPostProcessor = new ExtractPostProcessor();
        RegexExtract regexExtract = new RegexExtract();
        regexExtract.setExpressionMatchingRule("");
        JSONPathExtract jsonPathExtract = new JSONPathExtract();
        jsonPathExtract.setExpression("");
        XPathExtract xPathExtract = new XPathExtract();
        xPathExtract.setExpression("");
        extractPostProcessor.setExtractors(List.of(regexExtract, jsonPathExtract, xPathExtract));
        processors.add(extractPostProcessor);

        MsProcessorConfig msProcessorConfig = new MsProcessorConfig();
        msProcessorConfig.setProcessors(processors);

        msHTTPElement.setPreProcessorConfig(msProcessorConfig);
        msHTTPElement.setPostProcessorConfig(msProcessorConfig);
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
    }

    @Test
    public void msAssertionTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();

        List assertions = new ArrayList<>();

        ResponseCodeAssertion responseCodeAssertion = new ResponseCodeAssertion();
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setValue("value");
        responseCodeAssertion.setName("name");
        assertions.add(responseCodeAssertion);

        ResponseHeaderAssertion responseHeaderAssertion = new ResponseHeaderAssertion();
        ResponseHeaderAssertion.ResponseHeaderAssertionItem responseHeaderAssertionItem = new ResponseHeaderAssertion.ResponseHeaderAssertionItem();
        responseHeaderAssertionItem.setHeader("header");
        responseHeaderAssertionItem.setValue("value");
        responseHeaderAssertionItem.setCondition(MsAssertionCondition.EMPTY.name());
        responseHeaderAssertion.setAssertions(List.of(responseHeaderAssertionItem));
        assertions.add(responseHeaderAssertion);

        ResponseBodyAssertion responseBodyAssertion = new ResponseBodyAssertion();
        responseBodyAssertion.setAssertionType(MsBodyAssertionType.JSON_PATH.name());
        RegexAssertion regexAssertion = new RegexAssertion();
        regexAssertion.setAssertions(List.of(new RegexAssertionItem()));
        responseBodyAssertion.setRegexAssertion(regexAssertion);
        responseBodyAssertion.setDocumentAssertion(new DocumentAssertion());
        responseBodyAssertion.setJsonPathAssertion(new JSONPathAssertion());
        responseBodyAssertion.setXpathAssertion(new XPathAssertion());
        assertions.add(responseBodyAssertion);

        ResponseTimeAssertion responseTimeAssertion = new ResponseTimeAssertion();
        responseTimeAssertion.setMaxResponseTime(1000L);
        responseTimeAssertion.setEnable(true);
        responseTimeAssertion.setName("aa");
        assertions.add(responseTimeAssertion);

        ScriptAssertion scriptAssertion = new ScriptAssertion();
        scriptAssertion.setCommonScriptId("1111");
        scriptAssertion.setContent("1111");
        scriptAssertion.setDescription("1111");
        scriptAssertion.setName("1111");
        assertions.add(scriptAssertion);

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(assertions);
        msHTTPElement.setAssertionConfig(msAssertionConfig);
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);
    }

    public static MsHTTPElement getMsHttpElement() {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setUrl("http://www.test.com");
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        msHTTPElement.setName("name");
        msHTTPElement.setEnable(false);

        Header header = new Header();
        header.setEnable(false);
        header.setValue("value");
        header.setKey("key");
        header.setDescription("desc");
        msHTTPElement.setHeaders(List.of(header));

        RestParam restParam = new RestParam();
        restParam.setKey("key");
        restParam.setValue("value");
        restParam.setEnable(false);
        restParam.setDescription("desc");
        restParam.setRequired(true);
        msHTTPElement.setRest(List.of(restParam));

        QueryParam queryParam = new QueryParam();
        queryParam.setKey("key");
        queryParam.setValue("value");
        queryParam.setEnable(false);
        queryParam.setDescription("desc");
        queryParam.setRequired(true);
        msHTTPElement.setQuery(List.of(queryParam));

        MsHTTPConfig msHTTPConfig = new MsHTTPConfig();
        msHTTPConfig.setFollowRedirects(true);
        msHTTPConfig.setAutoRedirects(true);
        msHTTPConfig.setResponseTimeout(1000L);
        msHTTPConfig.setConnectTimeout(1000L);
        msHTTPConfig.setCertificateAlias("alias");
        msHTTPElement.setOtherConfig(msHTTPConfig);

        return msHTTPElement;
    }

    public static List<HttpResponse> getMsHttpResponse() {
        List<HttpResponse> httpResponses = new ArrayList<>();
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setName("Response1");
        httpResponse.setStatusCode("200");
        httpResponse.setDefaultFlag(true);

        Header header = new Header();
        header.setEnable(false);
        header.setValue("value");
        header.setKey("key");
        header.setDescription("desc");
        httpResponse.setHeaders(List.of(header));

        FormDataBody formDataBody = new FormDataBody();
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setEnable(false);
        formDataKV.setContentType("text/plain");
        formDataKV.setEncode(true);
        formDataKV.setMaxLength(10);
        formDataKV.setMinLength(8);
        formDataKV.setParamType("text");
        formDataKV.setDescription("test");
        formDataKV.setRequired(true);
        formDataKV.setValue("value");
        formDataKV.setKey("key");
        formDataBody.setFromValues(List.of(formDataKV));
        Body body = new Body();
        body.setBodyType(Body.BodyType.FORM_DATA.name());
        httpResponse.setBody(body);

        httpResponses.add(httpResponse);
        return httpResponses;
    }
}
