package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.*;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.assertion.*;
import io.metersphere.project.api.assertion.body.*;
import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import io.metersphere.project.api.processor.extract.JSONPathExtract;
import io.metersphere.project.api.processor.extract.RegexExtract;
import io.metersphere.project.api.processor.extract.ResultMatchingExtract;
import io.metersphere.project.api.processor.extract.XPathExtract;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.util.BeanUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:17
 */
public class MsHTTPElementTest {

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
        formDataKV.setMaxLength(10);
        formDataKV.setMinLength(8);
        formDataKV.setDescription("test");
        formDataKV.setRequired(true);
        formDataKV.setValue("@email");
        formDataKV.setKey("key");
        FormDataKV formDataFileKV = new FormDataKV();
        ApiFile bodyFile = new ApiFile();
        bodyFile.setFileId("aaa");
        bodyFile.setFileName("aaa");
        formDataFileKV.setFiles(List.of(bodyFile));
        formDataFileKV.setKey("fileKey");
        List<FormDataKV> formDataKVS = new ArrayList<>();
        formDataKVS.add(formDataFileKV);
        formDataBody.setFormValues(formDataKVS);
        body.setFormDataBody(formDataBody);

        WWWFormKV wwwFormKV = BeanUtils.copyBean(new WWWFormKV(), formDataKV);
        WWWFormBody wwwFormBody = new WWWFormBody();
        wwwFormBody.setFormValues(List.of(wwwFormKV));
        body.setWwwFormBody(wwwFormBody);

        JsonBody jsonBody = new JsonBody();
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setId("11");
        jsonBody.setJsonSchema(jsonSchemaItem);
        body.setJsonBody(jsonBody);

        body.setNoneBody(new NoneBody());

        RawBody rawBody = new RawBody();
        rawBody.setValue("A");
        body.setRawBody(rawBody);

        XmlBody xmlBody = new XmlBody();
        xmlBody.setValue("<a/>");
        body.setXmlBody(xmlBody);

        BinaryBody binaryBody = new BinaryBody();
        binaryBody.setFile(bodyFile);
        body.setBinaryBody(binaryBody);
        return body;
    }

    @Test
    public void processorParseTest() {

        MsHTTPElement msHTTPElement = getAddProcessorHttpElement();
        // 测试序列化
        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);

        // 测试脚本解析
        ParameterConfig parameterConfig = new ApiParamConfig();
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);
        defaultParser.parse(msTestElement, parameterConfig);
    }

    public static MsHTTPElement getAddProcessorHttpElement() {
        MsHTTPElement msHTTPElement = getMsHttpElement();

        List processors = new ArrayList<>();

        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setEnable(true);
        scriptProcessor.setScript("script");
        scriptProcessor.setScriptLanguage(ScriptLanguageType.JAVASCRIPT.name());
        processors.add(scriptProcessor);

        ScriptProcessor beanShellScriptProcessor = new ScriptProcessor();
        beanShellScriptProcessor.setEnable(true);
        beanShellScriptProcessor.setScript("script");
        beanShellScriptProcessor.setScriptLanguage(ScriptLanguageType.BEANSHELL.name());
        processors.add(beanShellScriptProcessor);

        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setScript("select * from user;");
        sqlProcessor.setName("sqlProcessor");
        KeyValueParam keyValueParam = new KeyValueParam();
        keyValueParam.setKey("id");
        keyValueParam.setValue("id_1");
        sqlProcessor.setEnable(true);
        sqlProcessor.setDataSourceId("dataSourceId");
        sqlProcessor.setExtractParams(List.of(keyValueParam));
        sqlProcessor.setResultVariable("ddd");
        sqlProcessor.setQueryTimeout(1111);
        sqlProcessor.setVariableNames("id,name");
        sqlProcessor.setDataSourceName("test");
        processors.add(sqlProcessor);
        SQLProcessor sqlProcessor1 = BeanUtils.copyBean(new SQLProcessor(), sqlProcessor);
        sqlProcessor1.setDataSourceId("1111");
        processors.add(sqlProcessor1);

        TimeWaitingProcessor timeWaitingProcessor = new TimeWaitingProcessor();
        timeWaitingProcessor.setDelay(1000L);
        timeWaitingProcessor.setEnable(true);
        processors.add(timeWaitingProcessor);

        List postProcessors = new ArrayList<>();

        ExtractPostProcessor extractPostProcessor = new ExtractPostProcessor();
        RegexExtract regexExtract = new RegexExtract();
        regexExtract.setVariableName("test");
        regexExtract.setExpressionMatchingRule("$1$");
        regexExtract.setExpression("test");

        RegexExtract regexExtract2 = new RegexExtract();
        regexExtract2.setVariableName("test");
        regexExtract2.setExpressionMatchingRule("$0$");
        regexExtract2.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.ALL.name());
        regexExtract2.setExtractScope("unescaped");
        regexExtract2.setExpression("test");

        JSONPathExtract jsonPathExtract = new JSONPathExtract();
        jsonPathExtract.setExpression("test");
        jsonPathExtract.setVariableName("test");
        jsonPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.RANDOM.name());

        XPathExtract xPathExtract = new XPathExtract();
        xPathExtract.setExpression("test");
        xPathExtract.setVariableName("test");
        xPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
        xPathExtract.setResultMatchingRuleNum(2);

        XPathExtract xPathExtract2 = new XPathExtract();
        xPathExtract2.setExpression("test");
        xPathExtract2.setVariableName("test");
        xPathExtract2.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
        xPathExtract2.setResultMatchingRuleNum(2);
        xPathExtract2.setResponseFormat(XPathExtract.ResponseFormat.HTML.name());

        extractPostProcessor.setExtractors(List.of(regexExtract, regexExtract2, jsonPathExtract, xPathExtract, xPathExtract2));
        postProcessors.addAll(processors);
        postProcessors.add(extractPostProcessor);

        MsProcessorConfig msProcessorConfig = new MsProcessorConfig();
        msProcessorConfig.setProcessors(processors);

        MsProcessorConfig msPostProcessorConfig = new MsProcessorConfig();
        msPostProcessorConfig.setProcessors(postProcessors);

        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setPreProcessorConfig(msProcessorConfig);
        msCommonElement.setPostProcessorConfig(msPostProcessorConfig);

        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement.setChildren(linkedList);

        return msHTTPElement;
    }

    @Test
    public void msAssertionTest() {

        MsHTTPElement msHTTPElement = getMsHttpElement();
        List<MsAssertion> assertions = getGeneralAssertions();

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(assertions);

        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setAssertionConfig(msAssertionConfig);

        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement.setChildren(linkedList);

        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);

        // 测试脚本解析
        ParameterConfig parameterConfig = new ApiParamConfig();
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);
        defaultParser.parse(msTestElement, parameterConfig);
    }

    @Test
    public void msAssertionTestXml() {

        MsHTTPElement msHTTPElement = getMsHttpElement();
        List<MsAssertion> assertions = getGeneralXmlAssertions();

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(assertions);

        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setAssertionConfig(msAssertionConfig);

        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement.setChildren(linkedList);

        String json = ApiDataUtils.toJSONString(msHTTPElement);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(ApiDataUtils.parseObject(json, AbstractMsTestElement.class), msHTTPElement);

        // 测试脚本解析
        ParameterConfig parameterConfig = new ApiParamConfig();
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);
        defaultParser.parse(msTestElement, parameterConfig);
    }

    public static List<MsAssertion> getGeneralAssertions() {
        List<MsAssertion> assertions = new ArrayList<>();
        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setExpectedValue("value");
        responseCodeAssertion.setName("name");
        assertions.add(responseCodeAssertion);

        MsResponseHeaderAssertion responseHeaderAssertion = new MsResponseHeaderAssertion();
        MsResponseHeaderAssertion.ResponseHeaderAssertionItem responseHeaderAssertionItem = new MsResponseHeaderAssertion.ResponseHeaderAssertionItem();
        responseHeaderAssertionItem.setHeader("header");
        responseHeaderAssertionItem.setExpectedValue("value");
        responseHeaderAssertionItem.setCondition(MsAssertionCondition.EMPTY.name());
        responseHeaderAssertion.setAssertions(List.of(responseHeaderAssertionItem));
        assertions.add(responseHeaderAssertion);

        MsResponseBodyAssertion regexResponseBodyAssertion = new MsResponseBodyAssertion();
        regexResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.REGEX.name());
        MsRegexAssertion regexAssertion = new MsRegexAssertion();

        MsRegexAssertionItem msRegexAssertionItem = new MsRegexAssertionItem();
        msRegexAssertionItem.setExpression("^test");
        regexAssertion.setAssertions(List.of(msRegexAssertionItem));
        regexResponseBodyAssertion.setRegexAssertion(regexAssertion);
        assertions.add(regexResponseBodyAssertion);

        MsResponseBodyAssertion documentResponseBodyAssertion = new MsResponseBodyAssertion();
        documentResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.DOCUMENT.name());
        MsDocumentAssertion msDocumentAssertion = new MsDocumentAssertion();
        MsDocumentAssertionElement item = new MsDocumentAssertionElement();
        item.setId("1");
        item.setParamName("pet");
        item.setInclude(false);
        item.setType("object");
        item.setCondition("none");
        item.setTypeVerification(false);
        item.setExpectedResult("");
        MsDocumentAssertionElement item1 = new MsDocumentAssertionElement();
        item1.setId("2");
        item1.setParamName("id");
        item1.setInclude(false);
        item1.setType("integer");
        item1.setCondition("EQUALS");
        item1.setExpectedResult("1");
        MsDocumentAssertionElement item2 = new MsDocumentAssertionElement();
        item2.setId("3");
        item2.setParamName("attributes");
        item2.setInclude(false);
        item2.setType("string");
        item2.setCondition("EQUALS");
        item2.setExpectedResult("s,2");
        MsDocumentAssertionElement item3 = new MsDocumentAssertionElement();
        item3.setId("4");
        item3.setParamName("array");
        item3.setInclude(false);
        item3.setArrayVerification(true);
        item3.setType("array");
        item3.setCondition("EQUALS");
        item3.setExpectedResult("s,2");
        item.setChildren(List.of(item1, item2, item3));
        msDocumentAssertion.setJsonAssertion(item);
        msDocumentAssertion.setDocumentType("JSON");
        documentResponseBodyAssertion.setDocumentAssertion(msDocumentAssertion);
        assertions.add(documentResponseBodyAssertion);

        MsResponseBodyAssertion jsonPathResponseBodyAssertion = new MsResponseBodyAssertion();
        jsonPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.JSON_PATH.name());
        MsJSONPathAssertion msJSONPathAssertion = new MsJSONPathAssertion();
        MsJSONPathAssertionItem msJSONPathAssertionItem = new MsJSONPathAssertionItem();
        msJSONPathAssertionItem.setExpression("^test");
        msJSONPathAssertionItem.setCondition(MsAssertionCondition.REGEX.name());
        msJSONPathAssertionItem.setExpectedValue("expectedValue");
        msJSONPathAssertion.setAssertions(List.of(msJSONPathAssertionItem));
        jsonPathResponseBodyAssertion.setJsonPathAssertion(msJSONPathAssertion);
        assertions.add(jsonPathResponseBodyAssertion);

        MsResponseBodyAssertion xpathPathResponseBodyAssertion = new MsResponseBodyAssertion();
        xpathPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.XPATH.name());
        MsXPathAssertion xPathPathAssertion = new MsXPathAssertion();
        xPathPathAssertion.setResponseFormat(MsXPathAssertion.ResponseFormat.XML.name());
        MsXPathAssertionItem xPathAssertionItem = new MsXPathAssertionItem();
        xPathAssertionItem.setExpression("^test");
        xPathAssertionItem.setExpectedValue("expectedValue");
        xPathPathAssertion.setAssertions(List.of(xPathAssertionItem));
        xpathPathResponseBodyAssertion.setXpathAssertion(xPathPathAssertion);
        assertions.add(xpathPathResponseBodyAssertion);

        MsResponseTimeAssertion responseTimeAssertion = new MsResponseTimeAssertion();
        responseTimeAssertion.setExpectedValue(1000L);
        responseTimeAssertion.setEnable(true);
        responseTimeAssertion.setName("aa");
        assertions.add(responseTimeAssertion);

        MsScriptAssertion scriptAssertion = new MsScriptAssertion();
        scriptAssertion.setCommonScriptInfo(new CommonScriptInfo());
        scriptAssertion.getCommonScriptInfo().setId("1111");
        scriptAssertion.setScript("1111");
        scriptAssertion.setName("1111");
        assertions.add(scriptAssertion);

        MsVariableAssertion msVariableAssertion = new MsVariableAssertion();
        MsVariableAssertion.VariableAssertionItem variableAssertionItem = new MsVariableAssertion.VariableAssertionItem();
        variableAssertionItem.setCondition(MsAssertionCondition.GT.name());
        variableAssertionItem.setExpectedValue("ev");
        variableAssertionItem.setVariableName("vn");
        msVariableAssertion.setVariableAssertionItems(List.of(variableAssertionItem));
        assertions.add(msVariableAssertion);
        return assertions;
    }

    public static List<MsAssertion> getGeneralXmlAssertions() {
        List<MsAssertion> assertions = new ArrayList<>();

        MsResponseBodyAssertion documentResponseBodyAssertion = new MsResponseBodyAssertion();
        documentResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.DOCUMENT.name());
        MsDocumentAssertion msDocumentAssertion = new MsDocumentAssertion();
        MsDocumentAssertionElement item = new MsDocumentAssertionElement();
        item.setId("1");
        item.setParamName("pet");
        item.setInclude(false);
        item.setType("object");
        item.setCondition("none");
        item.setTypeVerification(false);
        item.setExpectedResult("");
        MsDocumentAssertionElement item1 = new MsDocumentAssertionElement();
        item1.setId("2");
        item1.setParamName("id");
        item1.setInclude(false);
        item1.setType("integer");
        item1.setCondition("EQUALS");
        item1.setExpectedResult("1");
        MsDocumentAssertionElement item2 = new MsDocumentAssertionElement();
        item2.setId("3");
        item2.setParamName("attributes");
        item2.setInclude(false);
        item2.setType("string");
        item2.setCondition("EQUALS");
        item2.setExpectedResult("s,2");
        MsDocumentAssertionElement item3 = new MsDocumentAssertionElement();
        item3.setId("4");
        item3.setParamName("array");
        item3.setInclude(false);
        item3.setArrayVerification(true);
        item3.setType("array");
        item3.setCondition("EQUALS");
        item3.setExpectedResult("s,2");
        item.setChildren(List.of(item1, item2, item3));
        msDocumentAssertion.setXmlAssertion(item);
        msDocumentAssertion.setDocumentType("XML");
        documentResponseBodyAssertion.setDocumentAssertion(msDocumentAssertion);
        assertions.add(documentResponseBodyAssertion);

        return assertions;
    }

    public static MsHTTPElement getMsHttpElement() {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        msHTTPElement.setName("name");
        msHTTPElement.setEnable(true);

        MsHeader header = new MsHeader();
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

        MsHeader header = new MsHeader();
        header.setEnable(false);
        header.setValue("value");
        header.setKey("key");
        header.setDescription("desc");
        httpResponse.setHeaders(List.of(header));

        ResponseBody body = new ResponseBody();
        body.setBodyType(Body.BodyType.RAW.name());
        httpResponse.setBody(body);

        httpResponses.add(httpResponse);
        return httpResponses;
    }

    public static List<HttpResponse> get2MsHttpResponse(String returnPrefix) {
        List<HttpResponse> httpResponses = new ArrayList<>();
        HttpResponse http1Response = new HttpResponse();
        http1Response.setName("Response1");
        http1Response.setStatusCode("222");
        http1Response.setDefaultFlag(true);
        http1Response.setHeaders(new ArrayList<>() {{
            this.add(new MsHeader() {{
                this.setEnable(false);
                this.setValue("valueA1");
                this.setKey("keyA1");
                this.setDescription("descA1");
            }});
            this.add(new MsHeader() {{
                this.setEnable(true);
                this.setValue("headerDefaultValue");
                this.setKey("headerDefault");
                this.setDescription("headerDefaultDescA2");
            }});
        }});
        ResponseBody body1 = new ResponseBody();
        body1.setBodyType(Body.BodyType.RAW.name());
        body1.setRawBody(new RawBody() {{
            this.setValue(returnPrefix + "___responseDefault");
        }});
        http1Response.setBody(body1);
        httpResponses.add(http1Response);

        HttpResponse http2Response = new HttpResponse();
        http2Response.setName("Response2");
        http2Response.setStatusCode("222");
        http2Response.setDefaultFlag(false);
        http2Response.setHeaders(new ArrayList<>() {{
            this.add(new MsHeader() {{
                this.setEnable(false);
                this.setValue("valueB1");
                this.setKey("keyB1");
                this.setDescription("descB1");
            }});
            this.add(new MsHeader() {{
                this.setEnable(true);
                this.setValue("valueB2");
                this.setKey("keyB2");
                this.setDescription("descB2");
            }});
        }});
        ResponseBody body2 = new ResponseBody();
        body2.setBodyType(Body.BodyType.RAW.name());
        body2.setRawBody(new RawBody() {{
            this.setValue(returnPrefix + "___response2");
        }});
        http2Response.setBody(body2);

        httpResponses.add(http2Response);
        return httpResponses;
    }
}
