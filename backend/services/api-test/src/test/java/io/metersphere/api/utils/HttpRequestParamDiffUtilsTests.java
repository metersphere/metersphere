package io.metersphere.api.utils;

import io.metersphere.api.dto.definition.ApiCaseSyncItemRequest;
import io.metersphere.api.dto.definition.ApiCaseSyncRequest;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.XMLUtils;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.*;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-01  14:04
 */
public class HttpRequestParamDiffUtilsTests {
    @Test
    public void getBlankJon() {
        Object blankJon = HttpRequestParamDiffUtils.getBlankJon(JSON.parseObject("""
                {
                  "id": 10,
                  "name": "doggie",
                  "category": {
                    "id": null,
                    "name": "Dogs"
                  },
                  "photoUrls": [
                    "string"
                  ],
                  "tags": [
                    {
                      "id": 0,
                      "name": "string"
                    }
                  ],
                  "status": "available"
                }
                """));

        Object result = JSON.parseObject("""
                {
                  "id": "",
                  "name": "",
                  "category": {
                    "id": "",
                    "name": ""
                  },
                  "photoUrls": [
                    ""
                  ],
                  "tags": [
                    {
                      "id": "",
                      "name": ""
                    }
                  ],
                  "status": ""
                }""");

        Assertions.assertEquals(blankJon, result);
    }

    @Test
    public void getJsonKeys() {
        Set<String> jsonKeys = HttpRequestParamDiffUtils.getJsonKeys("""
                sdfds"key1"  :dsdfdsfd,sdfds"key2" 
                 :ddd,
                """);
        Assertions.assertEquals(jsonKeys, new HashSet<>(List.of("key1", "key2")));

        jsonKeys = HttpRequestParamDiffUtils.getJsonKeys("""
                sdfds"key1"  :dsdfdsfd,sdfds"key2" 
                 :ddd,
                """);
        Assertions.assertEquals(jsonKeys, new HashSet<>(List.of("key1", "key2")));


        jsonKeys = HttpRequestParamDiffUtils.getJsonKeys("""
                sdfds"key1 1111"  :dsdfdsfd,sdfds"key2-111" 
                 :ddd,
                """);
        Assertions.assertEquals(jsonKeys, new HashSet<>(List.of("key1 1111", "key2-111")));
    }

    @Test
    public void isJsonBodyDiff() {
        JsonBody jsonBody1 = new JsonBody();
        JsonBody jsonBody2 = new JsonBody();
        Assertions.assertFalse(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue("1111");
        jsonBody2.setJsonValue(null);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue(null);
        jsonBody2.setJsonValue("1111");
        Assertions.assertTrue(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue("""
                {"id1":"444"}
                """);

        jsonBody2.setJsonValue("""
                {"id1":"33"}
                """);
        Assertions.assertFalse(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue("""
                {"id1":"444"}ddd
                """);
        jsonBody2.setJsonValue("""
                {"id1":"33"}fff
                """);
        Assertions.assertFalse(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue("""
                {"id1":"444"}
                """);
        jsonBody2.setJsonValue("""
                {"id2":"33"}
                """);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));

        jsonBody1.setJsonValue("""
                {"id1":"444","id2":"33"}
                """);
        jsonBody2.setJsonValue("""
                {"id2":"33"}
                """);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isJsonBodyDiff(jsonBody1, jsonBody2));
    }

    @Test
    public void isXmlBodyDiff() {
        XmlBody xmlBody1 = new XmlBody();
        XmlBody xmlBody2 = new XmlBody();
        Assertions.assertFalse(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody1.setValue("1111");
        xmlBody2.setValue(null);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody2.setValue("1111");
        xmlBody1.setValue(null);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody1.setValue("""
                <tag1>11</tag1>
                """);
        xmlBody2.setValue("""
                <tag1>222</tag1>
                """);
        Assertions.assertFalse(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody1.setValue("""
                !@#$%^&*(
                """);
        xmlBody2.setValue("""
                !@#$%^&*(
                """);
        Assertions.assertFalse(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody1.setValue("""
                <tag1>11</tag1>
                """);
        xmlBody2.setValue("""
                <tag1>111</tag1>
                <tag2>2222</tag2>
                """);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));

        xmlBody1.setValue("""
                11111!@#$%^&*(
                """);
        xmlBody2.setValue("""
                !@#$%^&*(
                """);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isXmlBodyDiff(xmlBody1, xmlBody2));
    }

    @Test
    public void isParamKeyDiff() {
        Assertions.assertFalse(HttpRequestParamDiffUtils.isParamKeyDiff(null, null));
        Assertions.assertFalse(HttpRequestParamDiffUtils.isParamKeyDiff(null, new ArrayList<>()));
        Assertions.assertFalse(HttpRequestParamDiffUtils.isParamKeyDiff(new ArrayList<>(), null));

        List<KeyValueParam> params1 = new ArrayList<>();
        KeyValueParam kv1 = new KeyValueParam();
        kv1.setKey("key1");
        kv1.setValue("value1");
        params1.add(kv1);
        List<KeyValueParam> params2 = new ArrayList<>();
        KeyValueParam kv2 = new KeyValueParam();
        kv2.setKey("key1");
        kv2.setValue("value2");
        params2.add(kv2);
        Assertions.assertFalse(HttpRequestParamDiffUtils.isParamKeyDiff(params1, params2));

        params1 = new ArrayList<>();
        kv1 = new KeyValueParam();
        kv1.setKey("key1");
        kv1.setValue("value1");
        params1.add(kv1);
        params2 = new ArrayList<>();
        kv2 = new KeyValueParam();
        kv2.setKey("kv2");
        kv2.setValue("value2");
        params2.add(kv2);
        Assertions.assertTrue(HttpRequestParamDiffUtils.isParamKeyDiff(params1, params2));
    }

    @Test
    public void isBodyDiff() {
        Assertions.assertFalse(HttpRequestParamDiffUtils.isBodyDiff(null, null));
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(null, new Body()));
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(new Body(), null));

        Body body1 = new Body();
        Body body2 = new Body();

        body1.setBodyType(Body.BodyType.FORM_DATA.name());
        body1.setFormDataBody(new FormDataBody());
        body2.setBodyType(Body.BodyType.FORM_DATA.name());
        body2.setFormDataBody(new FormDataBody());
        Assertions.assertFalse(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));

        body1.setBodyType(Body.BodyType.FORM_DATA.name());
        body2.setBodyType(Body.BodyType.RAW.name());
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));

        body1.setBodyType(Body.BodyType.FORM_DATA.name());
        body1.setFormDataBody(new FormDataBody());
        FormDataKV formDataKV1 = new FormDataKV();
        formDataKV1.setKey("key1");
        formDataKV1.setValue("value");
        body1.getFormDataBody().getFormValues().add(formDataKV1);

        body2.setBodyType(Body.BodyType.FORM_DATA.name());
        body2.setFormDataBody(new FormDataBody());
        FormDataKV formDataKV2 = new FormDataKV();
        formDataKV2.setKey("key2");
        formDataKV2.setValue("value");
        body2.getFormDataBody().getFormValues().add(formDataKV2);
        // 校验 FORM_DATA
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));

        body1.setBodyType(Body.BodyType.WWW_FORM.name());
        body1.setWwwFormBody(new WWWFormBody());
        WWWFormKV wwwFormKV1 = new WWWFormKV();
        wwwFormKV1.setKey("key1");
        wwwFormKV1.setValue("value");
        body1.getWwwFormBody().getFormValues().add(wwwFormKV1);

        body2.setBodyType(Body.BodyType.WWW_FORM.name());
        body2.setWwwFormBody(new WWWFormBody());
        WWWFormKV wwwFormKV2 = new WWWFormKV();
        wwwFormKV2.setKey("key2");
        wwwFormKV2.setValue("value");
        body2.getWwwFormBody().getFormValues().add(wwwFormKV2);
        // 校验 WWW_FORM
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));

        body1.setBodyType(Body.BodyType.JSON.name());
        body1.setJsonBody(new JsonBody());
        body1.getJsonBody().setJsonValue("""
                {"id1":""}
                """);

        body2.setBodyType(Body.BodyType.JSON.name());
        body2.setJsonBody(new JsonBody());
        body2.getJsonBody().setJsonValue("""
                {"id2":""}
                """);
        // 校验 JSON
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));

        body1.setBodyType(Body.BodyType.XML.name());
        body1.setXmlBody(new XmlBody());
        body1.getXmlBody().setValue("""
                <tag1><tag1/>
                """);

        body2.setBodyType(Body.BodyType.XML.name());
        body2.setXmlBody(new XmlBody());
        body2.getXmlBody().setValue("""
                <tag2><tag2/>
                """);
        // 校验 XML
        Assertions.assertTrue(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));


        body1.setBodyType(Body.BodyType.RAW.name());
        body1.setRawBody(new RawBody());
        body1.getRawBody().setValue("value1");

        body2.setBodyType(Body.BodyType.RAW.name());
        body2.setRawBody(new RawBody());
        body2.getRawBody().setValue("value2");
        // 校验 RAW
        Assertions.assertFalse(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));


        body1.setBodyType(Body.BodyType.BINARY.name());
        body2.setBodyType(Body.BodyType.BINARY.name());
        // 校验 BINARY
        Assertions.assertFalse(HttpRequestParamDiffUtils.isBodyDiff(body1, body2));
    }

    @Test
    public void isRequestParamDiff() {
        Assertions.assertFalse(HttpRequestParamDiffUtils.isRequestParamDiff("", ""));

        MsHTTPElement msHTTPElement2 = new MsHTTPElement();
        MsHTTPElement msHTTPElement1 = new MsHTTPElement();
        QueryParam queryParam = new QueryParam();
        queryParam.setKey("111");
        msHTTPElement1.setQuery(List.of(queryParam));
        Assertions.assertTrue(HttpRequestParamDiffUtils.isRequestParamDiff(msHTTPElement1, msHTTPElement2));

        msHTTPElement1 = new MsHTTPElement();
        RestParam restParam = new RestParam();
        restParam.setKey("111");
        msHTTPElement1.setRest(List.of(restParam));
        Assertions.assertTrue(HttpRequestParamDiffUtils.isRequestParamDiff(msHTTPElement1, msHTTPElement2));

        msHTTPElement1 = new MsHTTPElement();
        MsHeader msHeader = new MsHeader();
        msHeader.setKey("111");
        msHTTPElement1.setHeaders(List.of(msHeader));
        Assertions.assertTrue(HttpRequestParamDiffUtils.isRequestParamDiff(msHTTPElement1, msHTTPElement2));

        msHTTPElement1 = new MsHTTPElement();
        msHTTPElement1.setBody(new Body());
        Assertions.assertTrue(HttpRequestParamDiffUtils.isRequestParamDiff(msHTTPElement1, msHTTPElement2));
    }

    @Test
    public void replaceIllegalJsonWithMock() {
        String replaceJon = HttpRequestParamDiffUtils.replaceIllegalJsonWithMock("""
                {
                  "id": "dddd",
                  "age": 10,
                  "name": @integer(1, 10),
                  "category": {
                    "id":@integer(1),
                    "name":  @integer,
                    "title":  @integer(1, 10)
                  }
                }
                """);

        Object result = JSON.parseObject("""
                {
                  "id": "dddd",
                  "age": 10,
                  "name": "",
                  "category": {
                    "id": "",
                    "name": "",
                    "title": ""
                  }
                }""");

        Assertions.assertEquals(JSON.parseObject(replaceJon), result);
    }

    @Test
    public void clearElementText() throws Exception {
        String xml = """
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>             
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.2.8</version>
                        <relativePath/>
                    </parent>
                </project>
                """;

        Element element = XMLUtils.stringToDocument(xml).getRootElement();
        XMLUtils.clearElementText(element);
        StringWriter stringWriter = new StringWriter();
        XMLWriter writer = new XMLWriter(stringWriter);
        writer.write(element);
        stringWriter.toString();

        String result = """
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"><modelVersion></modelVersion><parent><groupId></groupId><artifactId></artifactId><version></version><relativePath></relativePath></parent></project>""";

        Assertions.assertEquals(stringWriter.toString(), result);


        String regexResult = """
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion></modelVersion>             
                    <parent>
                        <groupId></groupId>
                        <artifactId></artifactId>
                        <version></version>
                        <relativePath/></parent>
                </project>
                """;
        Assertions.assertEquals(XMLUtils.clearElementText(xml), regexResult);
    }

    @Test
    public void getCompareHttpElement() {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        HttpRequestParamDiffUtils.getCompareHttpElement(msHTTPElement);

        msHTTPElement.setBody(new Body());
        Body body = msHTTPElement.getBody();
        body.setJsonBody(new JsonBody());
        body.setBodyType(Body.BodyType.RAW.name());
        HttpRequestParamDiffUtils.getCompareHttpElement(msHTTPElement);

        body.setBodyType(Body.BodyType.JSON.name());
        String jsonValue = """
                 {
                  "id": "dddd",
                  "age": 10,
                  "name": @integer(1, 10),
                  "category": {
                    "id":@integer(1),
                    "name":  @integer,
                    "title":  @integer(1, 10)
                  }
                }
                """;
        body.getJsonBody().setJsonValue(jsonValue);
        HttpRequestParamDiffUtils.getCompareHttpElement(msHTTPElement);
        Assertions.assertTrue(body.getJsonBody().getJsonValue().contains("\n"));
        Assertions.assertEquals(JSON.parseObject(body.getJsonBody().getJsonValue()), JSON.parseObject("""
                 {
                  "id": "",
                  "age": "",
                  "name": "",
                  "category": {
                    "id": "",
                    "name": "",
                    "title": ""
                  }
                }
                """));

        String xmlValue = """
                <project>
                    <modelVersion>4.0.0</modelVersion>             
                    <parent>
                        <groupId>org</groupId>
                        <artifactId>spring</artifactId>
                        <version>3.2.8</version>
                        <relativePath/>
                    </parent>
                </project>
                """;
        msHTTPElement.getBody().setBodyType(Body.BodyType.XML.name());
        body.setXmlBody(new XmlBody());
        body.getXmlBody().setValue(xmlValue);
        HttpRequestParamDiffUtils.getCompareHttpElement(msHTTPElement);


        xmlValue = """
                dx
                <project>
                    <modelVersion>4.0.0</modelVersion>             
                    <parent>
                        <groupId>org</groupId>
                        <artifactId>spring</artifactId>
                        <version>3.2.8</version>
                        <relativePath/>
                    </parent>
                </project>
                """;
        body.getXmlBody().setValue(xmlValue);
        HttpRequestParamDiffUtils.getCompareHttpElement(msHTTPElement);
    }

    @Test
    public void syncKeyValueParamDiff() {
        KeyValueParam kv1 = new KeyValueParam();
        kv1.setKey("key1");
        kv1.setValue("value1");
        KeyValueParam kv2 = new KeyValueParam();
        kv2.setKey("key2");
        kv2.setValue("value2");
        List<KeyValueParam> formDataKVS = List.of(kv1, kv2);

        List<KeyValueParam> result = HttpRequestParamDiffUtils.syncKeyValueParamDiff(true, formDataKVS, null);
        Assertions.assertEquals(result, formDataKVS);

        result = HttpRequestParamDiffUtils.syncKeyValueParamDiff(true, null, formDataKVS);
        Assertions.assertEquals(result, List.of());

        result = HttpRequestParamDiffUtils.syncKeyValueParamDiff(false, null, formDataKVS);
        Assertions.assertEquals(result, formDataKVS);

        KeyValueParam kv3 = new KeyValueParam();
        kv3.setKey("key3");
        kv3.setValue("value3");
        FormDataKV kv4 = new FormDataKV();

        result = HttpRequestParamDiffUtils.syncKeyValueParamDiff(true,
                new ArrayList<>(List.of(kv1, kv2, kv4)),
                new ArrayList<>(List.of(kv1, kv3, kv4)));
        Assertions.assertEquals(result, Arrays.asList(kv1, kv2, kv4));

        result = HttpRequestParamDiffUtils.syncKeyValueParamDiff(false,
                new ArrayList<>(List.of(kv1, kv2, kv4)),
                new ArrayList<>(List.of(kv1, kv3, kv4)));
        Assertions.assertEquals(result, Arrays.asList(kv1, kv3, kv2, kv4));
    }

    @Test
    public void syncJsonBodyDiff() {
        String sourceJsonStr = """
                {
                  "id": 10,
                  "name": "doggie",
                  "category": {
                    "id": null,
                    "name": "Dogs"
                  },
                  "photoUrls": [
                    "string",
                    {
                        "id": null,
                        "name": "Dogs"
                    }
                  ],
                  "tags": [
                    {
                      "id": 0,
                      "name": "string"
                    }
                  ]
                }
                """;
        String targetJsonStr = """
                {
                  "id": 11,
                  "delete": true,
                  "category": "",
                  "photoUrls": [
                    "string",
                    {
                        "id": "aaa",
                         "delete": true
                    }
                  ],
                  "tags": [
                    {
                      "id": "111",
                      "name": null
                    }
                  ]
                }
                """;
        Object sourceJson = JSON.parseObject(sourceJsonStr);
        Object targetJson = JSON.parseObject(targetJsonStr);
        Object result = HttpRequestParamDiffUtils.syncJsonBodyDiff(true, sourceJson, targetJson);
        Object assertionJson = JSON.parseObject("""
                 {
                  "id": 11,
                  "name": "doggie",
                  "category": {
                    "id": null,
                    "name": "Dogs"
                  },
                  "photoUrls": [
                    "string",
                    {
                        "id": null,
                        "name": "Dogs"
                    }
                  ],
                  "tags": [
                    {
                      "id": 0,
                      "name": "string"
                    }
                  ]
                }
                """);
        Assertions.assertEquals(result, assertionJson);

        JsonBody sourceJsonBody = new JsonBody();
        sourceJsonBody.setJsonValue(sourceJsonStr);
        JsonBody tartJsonBody = new JsonBody();
        tartJsonBody.setJsonValue(targetJsonStr);
        JsonBody resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(true, sourceJsonBody, tartJsonBody);
        Assertions.assertEquals(assertionJson, JSON.parseObject(resultBody.getJsonValue()));

        sourceJson = JSON.parseObject(sourceJsonStr);
        targetJson = JSON.parseObject(targetJsonStr);
        result = HttpRequestParamDiffUtils.syncJsonBodyDiff(false, sourceJson, targetJson);
        assertionJson = JSON.parseObject("""
                 {
                  "id": 11,
                  "name": "doggie",
                  "delete": true,
                  "category": {
                    "id": null,
                    "name": "Dogs"
                  },
                  "photoUrls": [
                    "string",
                    {
                        "id": "aaa",
                        "name": "Dogs",
                         "delete": true
                    }
                  ],
                  "tags": [
                    {
                      "id": 0,
                      "name": "string"
                    }
                  ]
                }
                """);
        Assertions.assertEquals(result, assertionJson);

        sourceJsonBody = new JsonBody();
        sourceJsonBody.setJsonValue(sourceJsonStr);
        tartJsonBody = new JsonBody();
        tartJsonBody.setJsonValue(targetJsonStr);
        resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(false, sourceJsonBody, tartJsonBody);
        Assertions.assertEquals(assertionJson, JSON.parseObject(resultBody.getJsonValue()));

        resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(false, null, tartJsonBody);
        Assertions.assertEquals(resultBody, tartJsonBody);

        resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(true, null, tartJsonBody);
        Assertions.assertEquals(resultBody, new JsonBody());

        resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(true, sourceJsonBody, null);
        Assertions.assertEquals(resultBody, sourceJsonBody);

        // 解析异常
        sourceJsonBody.setJsonValue("ddd");
        resultBody = HttpRequestParamDiffUtils.syncJsonBodyDiff(true, sourceJsonBody, sourceJsonBody);
        Assertions.assertEquals(resultBody, sourceJsonBody);
    }

    @Test
    public void syncXmlBodyDiff() throws Exception {
        String sourceXmlStr = """
                <project>
                    <modelVersion>4.0.0</modelVersion>   
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <relativePath/>
                    </parent>
                </project>
                """;
        String targetXmlStr = """
                <project>
                    <modelVersion>4.0.0</modelVersion>    
                    <delete>true</delete>  
                    <parent>
                        <groupId>org.springframework.boot</groupId>     
                        <relativePath/>
                        <delete>true</delete>  
                    </parent>
                </project>
                """;
        Element sourceElement = XMLUtils.stringToDocument(sourceXmlStr).getRootElement();
        Element targetElement = XMLUtils.stringToDocument(targetXmlStr).getRootElement();
        HttpRequestParamDiffUtils.syncXmlBodyDiff(true, null, sourceElement);
        HttpRequestParamDiffUtils.syncXmlBodyDiff(true, sourceElement, null);
        Element result = HttpRequestParamDiffUtils.syncXmlBodyDiff(true, sourceElement, targetElement);
        String resultStr = HttpRequestParamDiffUtils.parseElementToString(result);
        String assertionStr = HttpRequestParamDiffUtils.parseElementToString(XMLUtils.stringToDocument("""
                <project>
                    <modelVersion>4.0.0</modelVersion>
                   \s
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <relativePath/>
                       \s
                    <artifactId>spring-boot-starter-parent</artifactId></parent>
                </project>
                """).getRootElement());
        Assertions.assertEquals(resultStr, assertionStr);

        XmlBody sourceXmlBody = new XmlBody();
        XmlBody tartgetXmlBody = new XmlBody();
        sourceXmlBody.setValue(sourceXmlStr);
        tartgetXmlBody.setValue(targetXmlStr);
        XmlBody resultBody = HttpRequestParamDiffUtils.syncXmlBodyDiff(true, sourceXmlBody, tartgetXmlBody);
        Assertions.assertEquals(assertionStr, resultBody.getValue());

        sourceElement = XMLUtils.stringToDocument(sourceXmlStr).getRootElement();
        targetElement = XMLUtils.stringToDocument(targetXmlStr).getRootElement();
        result = HttpRequestParamDiffUtils.syncXmlBodyDiff(false, sourceElement, targetElement);
        resultStr = HttpRequestParamDiffUtils.parseElementToString(result);
        assertionStr = HttpRequestParamDiffUtils.parseElementToString(XMLUtils.stringToDocument("""
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <delete>true</delete>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <relativePath/>
                        <delete>true</delete>
                    <artifactId>spring-boot-starter-parent</artifactId></parent>
                </project>
                """).getRootElement());
        Assertions.assertEquals(resultStr, assertionStr);

        // 格式错误
        tartgetXmlBody.setValue("dddd");
        resultBody = HttpRequestParamDiffUtils.syncXmlBodyDiff(true, sourceXmlBody, tartgetXmlBody);
        Assertions.assertEquals(resultBody, tartgetXmlBody);
    }

    @Test
    public void syncBodyDiff() {

        Body sourceBody = new Body();
        Body targetBody = new Body();

        Body result = HttpRequestParamDiffUtils.syncBodyDiff(true, null, targetBody);
        Assertions.assertEquals(result, null);

        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, null);
        Assertions.assertEquals(result, sourceBody);

        sourceBody.setBodyType(Body.BodyType.FORM_DATA.name());
        targetBody.setBodyType(Body.BodyType.WWW_FORM.name());
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result, sourceBody);

        sourceBody.setBodyType(Body.BodyType.RAW.name());
        targetBody.setBodyType(Body.BodyType.RAW.name());
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result, targetBody);

        sourceBody.setBodyType(Body.BodyType.BINARY.name());
        targetBody.setBodyType(Body.BodyType.BINARY.name());
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result, targetBody);

        sourceBody.setBodyType(Body.BodyType.FORM_DATA.name());
        targetBody.setBodyType(Body.BodyType.FORM_DATA.name());
        sourceBody.setFormDataBody(new FormDataBody());
        targetBody.setFormDataBody(new FormDataBody());
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setKey("key1");
        formDataKV.setValue("value1");
        sourceBody.getFormDataBody().getFormValues().add(formDataKV);
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result.getFormDataBody(), sourceBody.getFormDataBody());
        FormDataKV formDataKV2 = new FormDataKV();
        formDataKV2.setKey("key2");
        formDataKV2.setValue("value2");
        targetBody.getFormDataBody().getFormValues().add(formDataKV);
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertNotEquals(result.getFormDataBody(), sourceBody.getFormDataBody());

        sourceBody.setBodyType(Body.BodyType.WWW_FORM.name());
        targetBody.setBodyType(Body.BodyType.WWW_FORM.name());
        sourceBody.setWwwFormBody(new WWWFormBody());
        targetBody.setWwwFormBody(new WWWFormBody());
        WWWFormKV wwwFormKV = new WWWFormKV();
        wwwFormKV.setKey("key1");
        wwwFormKV.setValue("value1");
        sourceBody.getWwwFormBody().getFormValues().add(wwwFormKV);
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result.getWwwFormBody(), sourceBody.getWwwFormBody());

        sourceBody.setBodyType(Body.BodyType.JSON.name());
        targetBody.setBodyType(Body.BodyType.JSON.name());
        sourceBody.setJsonBody(new JsonBody());
        targetBody.setJsonBody(new JsonBody());
        sourceBody.getJsonBody().setJsonValue("""
                {"id1":""}
                """);
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result.getJsonBody(), sourceBody.getJsonBody());

        sourceBody.setBodyType(Body.BodyType.XML.name());
        targetBody.setBodyType(Body.BodyType.XML.name());
        sourceBody.setXmlBody(new XmlBody());
        targetBody.setXmlBody(new XmlBody());
        sourceBody.getXmlBody().setValue("""
                <a></a>
                """);
        result = HttpRequestParamDiffUtils.syncBodyDiff(true, sourceBody, targetBody);
        Assertions.assertEquals(result.getXmlBody(), sourceBody.getXmlBody());
    }

    @Test
    public void syncRequestDiff() {
        MsHTTPElement sourceElement = new MsHTTPElement();
        MsHTTPElement targetElement = new MsHTTPElement();

        ApiCaseSyncRequest request = new ApiCaseSyncRequest();
        ApiCaseSyncItemRequest syncItems = new ApiCaseSyncItemRequest();
        request.setSyncItems(syncItems);
        request.setDeleteRedundantParam(true);

        AbstractMsTestElement resultTestElement = HttpRequestParamDiffUtils.syncRequestDiff(request, sourceElement, targetElement);
        Assertions.assertEquals(resultTestElement, targetElement);

        syncItems.setBody(false);
        syncItems.setQuery(false);
        syncItems.setRest(false);
        syncItems.setHeader(false);
        resultTestElement = HttpRequestParamDiffUtils.syncRequestDiff(request, sourceElement, targetElement);
        Assertions.assertEquals(resultTestElement, targetElement);

        resultTestElement = HttpRequestParamDiffUtils.syncRequestDiff(request, new MsLoopController(), targetElement);
        Assertions.assertEquals(resultTestElement, targetElement);
    }
}
