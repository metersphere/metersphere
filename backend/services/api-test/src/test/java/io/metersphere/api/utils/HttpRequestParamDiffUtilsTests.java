package io.metersphere.api.utils;

import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.sdk.util.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
