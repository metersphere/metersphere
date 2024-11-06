package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBinaryBody;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.mockserver.*;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.JsonBody;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.dto.request.http.body.XmlBody;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.metersphere.api.service.BaseResourcePoolTestService.DEFAULT_PROJECT_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
public class MockServerTestService {

    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;


    public static MockMultipartFile getMockMultipartFile(String fileName) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
    }

    public static MockMultipartFile getMockMultipartFile(String fileName, String fileContent) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                fileContent.getBytes()
        );
    }


    /**
     * 校验上传的文件
     *
     * @param id
     * @param fileIds 全部的文件ID
     */
    public static void assertUploadFile(String id, List<String> fileIds) throws Exception {
        if (fileIds != null) {
            ApiFileResourceService apiFileResourceService = CommonBeanFactory.getBean(ApiFileResourceService.class);
            // 验证文件的关联关系，以及是否存入对象存储
            assert apiFileResourceService != null;
            List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(id);
            Assertions.assertEquals(apiFileResources.size(), fileIds.size());

            String apiDefinitionDir = DefaultRepositoryDir.getApiMockDir(DEFAULT_PROJECT_ID, id);
            FileRequest fileRequest = new FileRequest();
            if (!fileIds.isEmpty()) {
                for (ApiFileResource apiFileResource : apiFileResources) {
                    Assertions.assertEquals(DEFAULT_PROJECT_ID, apiFileResource.getProjectId());
                    fileRequest.setFolder(apiDefinitionDir + "/" + apiFileResource.getFileId());
                    fileRequest.setFileName(apiFileResource.getFileName());
                    Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
                }
                fileRequest.setFolder(apiDefinitionDir);
            } else {
                fileRequest.setFolder(apiDefinitionDir);
                Assertions.assertTrue(CollectionUtils.isEmpty(FileCenter.getDefaultRepository().getFolderFileNames(fileRequest)));
            }
        }
    }

    /**
     * 校验上传的文件
     *
     * @param id
     * @param fileIds 全部的文件ID
     */
    public static void assertLinkFile(String id, List<String> fileIds) {
        FileAssociationService fileAssociationService = CommonBeanFactory.getBean(FileAssociationService.class);
        assert fileAssociationService != null;
        List<String> linkFileIds = fileAssociationService.getFiles(id)
                .stream()
                .map(FileInfo::getFileId)
                .toList();
        Assertions.assertEquals(fileIds, linkFileIds);
    }

    public ApiDefinitionMock assertAddApiDefinitionMock(Object request, MockMatchRule mockMatchRule, String id) {
        ApiDefinitionMock apiDefinitionMock = apiDefinitionMockMapper.selectByPrimaryKey(id);
        ApiDefinitionMockConfig apiDefinitionMockConfig = apiDefinitionMockConfigMapper.selectByPrimaryKey(id);
        ApiDefinitionMock copyApiDefinitionMock = BeanUtils.copyBean(new ApiDefinitionMock(), apiDefinitionMock);
        BeanUtils.copyBean(copyApiDefinitionMock, request);
        Assertions.assertEquals(apiDefinitionMock, copyApiDefinitionMock);
        if (apiDefinitionMockConfig != null) {
            Assertions.assertEquals(mockMatchRule, ApiDataUtils.parseObject(new String(apiDefinitionMockConfig.getMatching()), MockMatchRule.class));
        }
        return apiDefinitionMock;
    }

    public MockMatchRule genMockMatchRule(String valuePrefix, boolean hasQuery, boolean hasHeader, String bodyParamType, boolean matchAll) {
        MockMatchRule mockMatchRule = new MockMatchRule();

        KeyValueMatchRule restMatchRule = new KeyValueMatchRule();
        restMatchRule.setMatchAll(matchAll);
        restMatchRule.setMatchRules(new ArrayList<>() {{
            this.add(new KeyValueInfo() {{
                this.setKey("param1");
                this.setValue(valuePrefix + "__query-" + hasQuery + "_header-" + hasHeader);
            }});
            this.add(new KeyValueInfo() {{
                this.setKey("param2");
                this.setValue(valuePrefix + "-Param2");
            }});
        }});
        mockMatchRule.setRest(restMatchRule);

        if (hasQuery) {
            KeyValueMatchRule queryMatchRule = new KeyValueMatchRule();
            queryMatchRule.setMatchAll(matchAll);
            queryMatchRule.setMatchRules(new ArrayList<>() {{
                this.add(new KeyValueInfo() {{
                    this.setKey("queryParam1");
                    this.setValue(valuePrefix + "_queryParam1Value");
                }});
                this.add(new KeyValueInfo() {{
                    this.setKey("queryParam2");
                    this.setValue(valuePrefix + "_queryParam2Value");
                }});
                this.add(new KeyValueInfo() {{
                    this.setKey("queryParam3");
                    this.setValue(valuePrefix + "_queryParam3Value");
                }});
            }});
            mockMatchRule.setQuery(queryMatchRule);
        }

        if (hasHeader) {
            KeyValueMatchRule headerMatchRule = new KeyValueMatchRule();
            headerMatchRule.setMatchAll(matchAll);
            headerMatchRule.setMatchRules(new ArrayList<>() {{
                this.add(new KeyValueInfo() {{
                    this.setKey("headerA");
                    this.setValue(valuePrefix + "-header-1");
                }});
                this.add(new KeyValueInfo() {{
                    this.setKey("headerB");
                    this.setValue(valuePrefix + "-header-2");
                }});
                this.add(new KeyValueInfo() {{
                    this.setKey("headerC");
                    this.setValue(valuePrefix + "-header-3");
                }});
            }});
            mockMatchRule.setHeader(headerMatchRule);
        }

        if (StringUtils.equalsIgnoreCase(bodyParamType, "www")) {
            mockMatchRule.setBody(new BodyParamMatchRule() {{
                this.setBodyType(Body.BodyType.WWW_FORM.name());
                this.setWwwFormBody(new KeyValueMatchRule() {{
                    this.setMatchAll(matchAll);
                    this.setMatchRules(new ArrayList<>() {{
                        this.add(new KeyValueInfo() {{
                            this.setKey("bodyKvParam1");
                            this.setValue(valuePrefix + "_bodyKvParam1");
                        }});
                        this.add(new KeyValueInfo() {{
                            this.setKey("bodyParam2");
                            this.setValue(valuePrefix + "_bodyKvParam2");
                        }});
                        this.add(new KeyValueInfo() {{
                            this.setKey("bodyParam3");
                            this.setValue(valuePrefix + "_bodyKvParam3");
                        }});
                    }});
                }});
            }});
        } else if (StringUtils.equalsIgnoreCase(bodyParamType, "raw")) {
            mockMatchRule.setBody(new BodyParamMatchRule() {{
                this.setBodyType(Body.BodyType.RAW.name());
                this.getRawBody().setValue(valuePrefix + "_inputRawBody");
            }});
        } else if (StringUtils.equalsIgnoreCase(bodyParamType, "json")) {
            mockMatchRule.setBody(new BodyParamMatchRule() {{
                this.setBodyType(Body.BodyType.JSON.name());
                this.getJsonBody().setJsonValue("{\"inputAge\":123}");
            }});
        } else if (StringUtils.equalsIgnoreCase(bodyParamType, "xml")) {
            mockMatchRule.setBody(new BodyParamMatchRule() {{
                this.setBodyType(Body.BodyType.XML.name());
                this.getXmlBody().setValue("<xml>input123</xml>");
            }});
        } else if (StringUtils.equalsIgnoreCase(bodyParamType, "kv")) {
            mockMatchRule.setBody(new BodyParamMatchRule() {{
                this.setBodyType(Body.BodyType.FORM_DATA.name());
                this.setFormDataBody(new MockFormDataBody() {{
                    this.setMatchAll(matchAll);
                    this.setMatchRules(new ArrayList<>() {{
                        this.add(new FormKeyValueInfo() {{
                            this.setKey("bodyKvParam1");
                            this.setValue(valuePrefix + "_bodyKvParam1");
                        }});
                        this.add(new FormKeyValueInfo() {{
                            this.setKey("bodyParam2");
                            this.setValue(valuePrefix + "_bodyKvParam2");
                        }});
                        this.add(new FormKeyValueInfo() {{
                            this.setKey("bodyParam3");
                            this.setValue(valuePrefix + "_bodyKvParam3");
                        }});
                    }});
                }});
            }});
        }
        return mockMatchRule;
    }

    public MockResponse genMockResponse(String returnType, int status, String valueKeyWord, String fileId, String fileName, ApiDefinitionBlob apiDefinitionBlob) {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setStatusCode(status);

        if (apiDefinitionBlob != null) {
            mockResponse.setUseApiResponse(true);
            List<HttpResponse> msHttpResponseList = JSON.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class);
            msHttpResponseList.stream()
                    .filter(item -> !item.isDefaultFlag())
                    .findFirst()
                    .ifPresent(item -> mockResponse.setApiResponseId(item.getId()));
        } else {
            ResponseBody body = new ResponseBody();

            if ("file".equals(returnType) || "file-body".equals(returnType)) {
                body.setBodyType(Body.BodyType.BINARY.name());
                body.setBinaryBody(new ResponseBinaryBody() {{
                    setSendAsBody(false);
                    setFile(new ApiFile());
                    getFile().setFileId(fileId);
                    getFile().setFileName(fileName);
                }});
            } else if ("json".equals(returnType)) {
                body.setBodyType(Body.BodyType.JSON.name());
                body.setJsonBody(new JsonBody() {{
                    setJsonValue("{\"inputAge\":123, \"testKeyWord\":\"" + valueKeyWord + "\"}");
                }});
            } else if ("xml".equals(returnType)) {
                body.setBodyType(Body.BodyType.XML.name());
                body.setXmlBody(new XmlBody() {{
                    setValue("<xml>" + valueKeyWord + "</xml>");
                }});
            } else if ("raw".equals(returnType)) {
                body.setBodyType(Body.BodyType.RAW.name());
                body.setRawBody(new RawBody() {{
                    setValue("Raw body content:" + valueKeyWord);
                }});
            }

            mockResponse.setBody(body);
        }

        List<MsHeader> headers = List.of(
                new MsHeader() {{
                    this.setKey("rspHeaderA");
                    this.setValue("header-1");
                }},
                new MsHeader() {{
                    this.setKey("rspHeaderB");
                    this.setValue("header-2");
                    this.setEnable(false);
                }},
                new MsHeader() {{
                    this.setKey("rspHeaderC");
                    this.setValue("header-3");
                }}
        );
        mockResponse.setHeaders(headers);

        return mockResponse;
    }

    public MockHttpServletRequestBuilder getRequestBuilder(String method, String url) {
        MockHttpServletRequestBuilder requestBuilder = null;
        if (StringUtils.equalsIgnoreCase(method, "get")) {
            requestBuilder = MockMvcRequestBuilders.get(url);
        } else if (StringUtils.equalsIgnoreCase(method, "post")) {
            requestBuilder = MockMvcRequestBuilders.post(url);
        } else if (StringUtils.equalsIgnoreCase(method, "put")) {
            requestBuilder = MockMvcRequestBuilders.put(url);
        } else if (StringUtils.equalsIgnoreCase(method, "delete")) {
            requestBuilder = MockMvcRequestBuilders.delete(url);
        } else if (StringUtils.equalsIgnoreCase(method, "patch")) {
            requestBuilder = MockMvcRequestBuilders.patch(url);
        } else if (StringUtils.equalsIgnoreCase(method, "head")) {
            requestBuilder = MockMvcRequestBuilders.head(url);
        } else if (StringUtils.equalsIgnoreCase(method, "options")) {
            requestBuilder = MockMvcRequestBuilders.options(url);
        } else if (StringUtils.equalsIgnoreCase(method, "trace")) {
            requestBuilder = MockMvcRequestBuilders.request(HttpMethod.TRACE, url);
        }
        return requestBuilder;
    }

    @Resource
    private MockMvc mockMvc;

    public void testNoMatchMockConfig(String method, String url, String apiDefinitionPath) throws Exception {

        url = StringUtils.replace(url, "{param1}", "param1");
        url = StringUtils.replace(url, "{param2}", "param2");
        MockHttpServletRequestBuilder requestBuilder;
        if (StringUtils.equalsAnyIgnoreCase(method, "get", "delete")) {
            requestBuilder = getRequestBuilder(method, url + IDGenerator.nextStr());
            Assertions.assertNotNull(requestBuilder);
        } else {
            requestBuilder = getRequestBuilder(method, url);
        }
        ResultActions action = mockMvc.perform(requestBuilder);
        MvcResult mockResult = action.andReturn();
        String returnStr = mockResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertEquals(returnStr, apiDefinitionPath + "___responseDefault");
        Assertions.assertEquals(mockResult.getResponse().getStatus(), 222);
    }

    public void testNoMatchApi() throws Exception {
        String url = "/mock-server/100001/" + "error" + "/test/error";
        this.testApiNoMockConfigAndNoResponse(url);
    }

    public void testApiNoMockConfigAndNoResponse(String url) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = getRequestBuilder("get", url);
        ResultActions action = mockMvc.perform(requestBuilder);
        action.andExpect(status().isNotFound());
        MvcResult mockResult = action.andReturn();
        String returnStr = mockResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertEquals(returnStr, Translator.get("mock_warning"));
    }
}
