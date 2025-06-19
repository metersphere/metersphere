package io.metersphere.api.utils;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.EnumValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ApiTestCaseDTOParser {

    public static ApiTestCaseDTO parse(String id, String content) {
        ApiTestCaseDTO caseDTO = new ApiTestCaseDTO();
        String[] lines = content.split("\\n");

        MsHTTPElement msHTTPElement = new MsHTTPElement();
        MsCommonElement processorConfig = new MsCommonElement();

        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        List<MsAssertion> assertions = new ArrayList<>();

        //请求头
        List<MsHeader> headers = new ArrayList<>();
        // Query参数
        List<QueryParam> query = new ArrayList<>();
        // Rest参数
        List<RestParam> restParams = new ArrayList<>();
        // 请求体
        Body body = new Body();
        FormDataBody formDataBody = new FormDataBody();
        WWWFormBody wwwFormBody = new WWWFormBody();
        XmlBody xmlBody = new XmlBody();
        JsonBody jsonBody = new JsonBody();
        RawBody rawBody = new RawBody();
        NoneBody noneBody = new NoneBody();
        BinaryBody binaryBody = new BinaryBody();


        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 用例名称
            Pattern namePattern = Pattern.compile("##\\s*用例名称\\s*[：:]\\s*(.+)");
            Matcher nameMatcher = namePattern.matcher(line);
            if (nameMatcher.find()) {
                msHTTPElement.setName(nameMatcher.group(1).trim());
                i++; // 跳过名称行
            }

            // 请求头
            Pattern headersPattern = Pattern.compile("^##\\s*请求头\\s*(.*)$", Pattern.MULTILINE);
            Matcher headersMatcher = headersPattern.matcher(line);
            if (headersMatcher.find()) {
                i = parseHeaders(headers, lines, i + 1);
            }
            // Query参数
            Pattern queryPattern = Pattern.compile("^##\\s*Query参数\\s*(.*)$", Pattern.MULTILINE);
            Matcher queryMatcher = queryPattern.matcher(line);
            if (queryMatcher.find()) {
                i = parseQuery(query, lines, i + 1);
            }

            // Rest参数
            Pattern restPattern = Pattern.compile("^##\\s*Rest参数\\s*(.*)$", Pattern.MULTILINE);
            Matcher restMatcher = restPattern.matcher(line);
            if (restMatcher.find()) {
                i = parseRest(restParams, lines, i + 1);
            }

            // 请求体
            Pattern bodyPattern = Pattern.compile("^##\\s*请求体\\s*(.*)$", Pattern.MULTILINE);
            Matcher bodyMatcher = bodyPattern.matcher(line);
            if (bodyMatcher.find()) {
                i = parseBody(body, lines, i, content, formDataBody, wwwFormBody, xmlBody, jsonBody, rawBody);
            }


            // 断言
            Pattern assertionsPattern = Pattern.compile("^##\\s*断言\\s*(.*)$", Pattern.MULTILINE);
            Matcher assertionsMatcher = assertionsPattern.matcher(line);
            if (assertionsMatcher.find()) {
                i = parseAssertions(assertions, lines, i);
            }
        }


        body.setFormDataBody(formDataBody);
        body.setWwwFormBody(wwwFormBody);
        body.setXmlBody(xmlBody);
        body.setJsonBody(jsonBody);
        body.setRawBody(rawBody);
        body.setNoneBody(noneBody);
        body.setBinaryBody(binaryBody);
        msHTTPElement.setHeaders(headers);
        msHTTPElement.setQuery(query);
        msHTTPElement.setRest(restParams);
        msHTTPElement.setBody(body);

        msAssertionConfig.setAssertions(assertions);
        processorConfig.setAssertionConfig(msAssertionConfig);

        //构建返回参数
        buildReturnDto(caseDTO, msHTTPElement, processorConfig, id);
        return caseDTO;
    }

    private static void buildReturnDto(ApiTestCaseDTO caseDTO, MsHTTPElement msHTTPElement, MsCommonElement processorConfig, String id) {
        ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        ApiDefinitionBlobMapper apiDefinitionBlobMapper = CommonBeanFactory.getBean(ApiDefinitionBlobMapper.class);
        ApiCommonService apiCommonService = CommonBeanFactory.getBean(ApiCommonService.class);
        //接口定义信息
        ApiDefinition apiDefinition = apiTestCaseService.getApiDefinition(id);
        Optional<ApiDefinitionBlob> apiDefinitionBlobOptional = Optional.ofNullable(apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId()));

        msHTTPElement.setMethod(apiDefinition.getMethod());

        apiDefinitionBlobOptional.ifPresent(blob -> {
            AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
            MsCommonElement apimsCommonElement = apiCommonService.getMsCommonElement(msTestElement);
            Optional.ofNullable(apimsCommonElement).ifPresent(item -> {
                processorConfig.setPreProcessorConfig(item.getPreProcessorConfig());
                processorConfig.setPostProcessorConfig(item.getPostProcessorConfig());
            });

            //接口定义json_schema
            if (msTestElement instanceof MsHTTPElement) {
                MsHTTPElement apiMsHTTPElement = (MsHTTPElement) msTestElement;
                if (StringUtils.equals(apiMsHTTPElement.getBody().getBodyType(), Body.BodyType.JSON.name()) && apiMsHTTPElement.getBody().getJsonBody().getEnableJsonSchema()) {
                    msHTTPElement.getBody().getJsonBody().setJsonSchema(apiMsHTTPElement.getBody().getJsonBody().getJsonSchema());
                }

            }
        });
        LinkedList<AbstractMsTestElement> children = new LinkedList<>();
        children.add(processorConfig);
        msHTTPElement.setChildren(children);

        caseDTO.setRequest(msHTTPElement);
        caseDTO.setName(msHTTPElement.getName());
        caseDTO.setModuleId(apiDefinition.getModuleId());
        caseDTO.setMethod(apiDefinition.getMethod());
        caseDTO.setNum(apiDefinition.getNum());
        caseDTO.setProtocol(apiDefinition.getProtocol());
        caseDTO.setAiCreate(true);
    }


    private static int parseQuery(List<QueryParam> query, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;

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
        return i;
    }

    private static int parseRest(List<RestParam> restParams, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;

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
        return i;
    }


    private static int parseHeaders(List<MsHeader> headers, String[] lines, int startIndex) {
        // 跳过表头行和分隔行
        int i = startIndex + 2;

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;


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
        return i;
    }

    private static int parseBody(Body body, String[] lines, int startIndex, String content,
                                 FormDataBody formDataBody,
                                 WWWFormBody wwwFormBody,
                                 XmlBody xmlBody,
                                 JsonBody jsonBody,
                                 RawBody rawBody) {
        // 获取请求体类型
        for (int i = startIndex + 1; i < lines.length; i++) {
            String typeLine = lines[i].trim();
            if (typeLine.contains("**请求体类型")) {
                Pattern pattern = Pattern.compile("\\*\\*请求体类型\\s*[：:]\\s*(.*?)\\s*\\*\\*");
                Matcher matcher = pattern.matcher(typeLine);
                if (matcher.find()) {
                    String bodyType = matcher.group(1).trim();
                    switch (bodyType) {
                        case "form-data" -> {
                            body.setBodyType(Body.BodyType.FORM_DATA.name());
                            startIndex = i + 3;
                        }
                        case "x-www-form-urlencoded" -> {
                            body.setBodyType(Body.BodyType.WWW_FORM.name());
                            startIndex = i + 3;
                        }
                        case "json" -> body.setBodyType(Body.BodyType.JSON.name());
                        case "xml" -> body.setBodyType(Body.BodyType.XML.name());
                        case "raw" -> body.setBodyType(Body.BodyType.RAW.name());
                        default -> {
                        }
                    }
                }
                break;
            }
        }


        // 跳过表头行和分隔行
        int i = startIndex;

        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;

            // 格式:
            String[] parts = line.split("\\|");

            //判断请求体类型 todo
            Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body.getBodyType());
            switch (bodyType) {
                case FORM_DATA -> {
                    if (parts.length >= 6) {
                        FormDataKV formDataKV = new FormDataKV();
                        formDataKV.setKey(parts[1].trim());
                        formDataKV.setParamType(parts[2].trim());
                        formDataKV.setValue(parts[3].trim());
                        formDataKV.setContentType(parts[4].trim());
                        formDataKV.setDescription(parts[5].trim());
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
                    Pattern pattern = Pattern.compile("```xml\\s*([\\s\\S]*?)\\s*```");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        xmlBody.setValue(matcher.group(1));
                    }
                }
                case JSON -> {
                    Pattern pattern = Pattern.compile("```json\\s*(\\{[\\s\\S]*?})\\s*```");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        jsonBody.setJsonValue(matcher.group(1));
                    }
                }
                case RAW -> {
                    Pattern pattern = Pattern.compile("```tex\\s*([\\s\\S]*?)\\s*```");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        rawBody.setValue(matcher.group(1));
                    }
                }
                case NONE -> {
                }
                default -> {
                }
            }
            i++;
        }
        return i;
    }

    private static int parseAssertions(List<MsAssertion> assertions, String[] lines, int startIndex) {
        int i = startIndex + 1;


        while (i < lines.length) {
            String line = lines[i].trim();

            Pattern pattern = Pattern.compile(".*?(?:状态码|响应码)[^\\d]*(\\d{3})");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                MsResponseCodeAssertion codeAssertion = new MsResponseCodeAssertion();
                codeAssertion.setCondition(MsAssertionCondition.EQUALS.name());
                codeAssertion.setExpectedValue(matcher.group(1) != null ? matcher.group(1) : "");
                codeAssertion.setName("状态码");
                assertions.add(codeAssertion);
            }
            i++;
        }
        return i;
    }
}