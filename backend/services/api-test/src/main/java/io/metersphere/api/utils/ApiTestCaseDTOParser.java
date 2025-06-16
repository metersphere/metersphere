package io.metersphere.api.utils;

import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.definition.ApiTestCaseAiDTO;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.EnumValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ApiTestCaseDTOParser {

    public static ApiTestCaseAiDTO parse(String content) {
        ApiTestCaseAiDTO testCase = new ApiTestCaseAiDTO();
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        MsCommonElement processorConfig = new MsCommonElement();
        String[] lines = content.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 用例名称
            if (line.equals("## 用例名称")) {
                msHTTPElement.setName(lines[i + 1].trim());
                i++; // 跳过名称行
            }
            // 请求头
            else if (line.equals("## 请求头")) {
                i = parseHeaders(msHTTPElement, lines, i + 1);
            }
            // Query参数
            else if (line.equals("## Query参数")) {
                i = parseQuery(msHTTPElement, lines, i + 1);
            }
            // Rest参数
            else if (line.equals("## Rest参数")) {
                i = parseRest(msHTTPElement, lines, i + 1);
            }

            // 请求体
            else if (line.equals("## 请求体")) {
                i = parseBody(msHTTPElement, lines, i);
            }
            // 断言
            else if (line.equals("## 断言")) {
                i = parseAssertions(processorConfig, lines, i);
            }
        }

        testCase.setMsHTTPElement(msHTTPElement);
        testCase.setProcessorConfig(processorConfig);
        return testCase;
    }


    private static int parseQuery(MsHTTPElement msHTTPElement, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        List<QueryParam> query = new ArrayList<>();

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;
            // 格式:
            line = "|" + line + " |";

            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                QueryParam queryParam = new QueryParam();
                queryParam.setKey(parts[1].trim());
                queryParam.setParamType(parts[2].trim());
                queryParam.setValue(parts[3].trim());
                queryParam.setDescription(parts[4].trim());
                query.add(queryParam);
            }
            i++;
        }
        msHTTPElement.setQuery(query);
        return i;
    }

    private static int parseRest(MsHTTPElement msHTTPElement, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        List<RestParam> restParams = new ArrayList<>();

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;
            // 格式:
            line = "|" + line + " |";

            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                RestParam restParam = new RestParam();
                restParam.setKey(parts[1].trim());
                restParam.setParamType(parts[2].trim());
                restParam.setValue(parts[3].trim());
                restParam.setDescription(parts[4].trim());
                restParams.add(restParam);
            }
            i++;
        }
        msHTTPElement.setRest(restParams);
        return i;
    }


    private static int parseHeaders(MsHTTPElement msHTTPElement, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        List<MsHeader> headers = new ArrayList<>();
        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;

            // 格式:
            line = "|" + line + " |";


            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                MsHeader msHeader = new MsHeader();
                msHeader.setKey(parts[1].trim());
                msHeader.setValue(parts[2].trim());
                msHeader.setDescription(parts[3].trim());
                headers.add(msHeader);
            }
            i++;
        }
        msHTTPElement.setHeaders(headers);
        return i;
    }

    private static int parseBody(MsHTTPElement msHTTPElement, String[] lines, int startIndex) {
        // 获取请求体类型
        String typeLine = lines[startIndex + 1].trim();
        String[] typeParts = typeLine.split("\\|");
        if (typeParts.length >= 2) {
            Body body = new Body();
            String bodyType = typeParts[1].trim();
            switch (bodyType) {
                case "form-data" -> body.setBodyType(Body.BodyType.FORM_DATA.name());
                case "x-www-form-urlencoded" -> body.setBodyType(Body.BodyType.WWW_FORM.name());
                case "json" -> body.setBodyType(Body.BodyType.JSON.name());
                case "xml" -> body.setBodyType(Body.BodyType.XML.name());
                case "raw" -> body.setBodyType(Body.BodyType.RAW.name());
                default -> throw new MSException("请求体类型错误：" + bodyType);
            }
            msHTTPElement.setBody(body);
        }

        FormDataBody formDataBody = new FormDataBody();
        WWWFormBody wwwFormBody = new WWWFormBody();
        XmlBody xmlBody = new XmlBody();
        JsonBody jsonBody = new JsonBody();
        RawBody rawBody = new RawBody();
        NoneBody noneBody = new NoneBody();

        // 跳过表头行和分隔行
        int i = startIndex + 4;

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;

            line = "|" + line + " |";
            // 格式:
            String[] parts = line.split("\\|");

            //判断请求体类型 todo
            Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, msHTTPElement.getBody().getBodyType());
            switch (bodyType) {
                case FORM_DATA -> {
                    if (parts.length >= 5) {
                        FormDataKV formDataKV = new FormDataKV();
                        formDataKV.setKey(parts[1].trim());
                        formDataKV.setParamType(parts[2].trim());
                        formDataKV.setValue(parts[3].trim());
                        formDataKV.setDescription(parts[4].trim());
                        formDataBody.getFormValues().add(formDataKV);
                    }
                }
                case WWW_FORM -> {
                    if (parts.length >= 5) {
                        WWWFormKV wwwFormKV = new WWWFormKV();
                        wwwFormKV.setKey(parts[1].trim());
                        wwwFormKV.setParamType(parts[2].trim());
                        wwwFormKV.setValue(parts[3].trim());
                        wwwFormKV.setDescription(parts[4].trim());
                        wwwFormBody.getFormValues().add(wwwFormKV);
                    }
                }
                case XML -> {
                    xmlBody.setValue(parts[1].trim());
                }
                case JSON -> {
                    jsonBody.setJsonValue(parts[1].trim());
                }
                case RAW -> {
                    rawBody.setValue(parts[1].trim());
                }
                case NONE -> {
                }
                default -> {
                }
            }
            i++;
        }

        msHTTPElement.getBody().setFormDataBody(formDataBody);
        msHTTPElement.getBody().setWwwFormBody(wwwFormBody);
        msHTTPElement.getBody().setXmlBody(xmlBody);
        msHTTPElement.getBody().setJsonBody(jsonBody);
        msHTTPElement.getBody().setRawBody(rawBody);
        msHTTPElement.getBody().setNoneBody(noneBody);
        return i;
    }

    private static int parseAssertions(MsCommonElement processorConfig, String[] lines, int startIndex) {
        int i = startIndex + 1;

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        List<MsAssertion> assertions = msAssertionConfig.getAssertions();

        while (i < lines.length) {
            String line = lines[i].trim();

            Pattern pattern = Pattern.compile("(?:.*?状态码[:：]\\s*(\\d{3})\\b|\\b(\\d{3})\\b)");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                MsResponseCodeAssertion codeAssertion = new MsResponseCodeAssertion();
                codeAssertion.setCondition(MsAssertionCondition.EQUALS.name());
                codeAssertion.setExpectedValue(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
                assertions.add(codeAssertion);
            }
            i++;
        }
        processorConfig.setAssertionConfig(msAssertionConfig);
        return i;
    }

}