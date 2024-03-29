package io.metersphere.api.service.mockserver;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.mockserver.HttpRequestParam;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.MockServerUtils;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.service.FileManagementService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class MockServerService {

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    FileMetadataMapper fileMetadataMapper;
    @Resource
    FileManagementService fileManagementService;

    private boolean isUrlParamMethod(String method) {
        return StringUtils.equalsAnyIgnoreCase(method, HttpMethodConstants.GET.name(), HttpMethodConstants.DELETE.name(), HttpMethodConstants.OPTIONS.name(), HttpMethodConstants.HEAD.name());
    }


    public Object execute(String method, Map<String, String> requestHeaderMap, String projectNum, String apiNumInfo, HttpServletRequest request, HttpServletResponse response) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.selectByProjectNumAndApiNum(projectNum, apiNumInfo);
        String url = request.getRequestURL().toString();
        String requestUrlSuffix = MockServerUtils.getUrlSuffix(StringUtils.joinWith("/", "/mock-server", projectNum, apiNumInfo), request);
        if (apiDefinition == null) {
            requestUrlSuffix = MockServerUtils.getUrlSuffix(StringUtils.joinWith("/", "/mock-server", projectNum), request);
            apiDefinition = this.selectByProjectNumAndUrl(projectNum, method, requestUrlSuffix);
            if (apiDefinition == null) {
                return this.requestNotFound(response);
            }
        }

        if (StringUtils.equalsIgnoreCase(method, apiDefinition.getMethod()) && !MockServerUtils.checkUrlMatch(apiDefinition.getPath(), requestUrlSuffix)) {
            return this.requestNotFound(response);
        }
        HttpRequestParam requestMockParams = MockServerUtils.getHttpRequestParam(request, requestUrlSuffix, apiDefinition.getPath(), !this.isUrlParamMethod(method));
        LogUtils.info("Mock [" + url + "] Header:{}", requestHeaderMap);
        LogUtils.info("Mock [" + url + "] request:{}", JSON.toJSONString(requestMockParams));
        ApiDefinitionMockConfig compareMockConfig = this.match(apiDefinition.getId(), requestHeaderMap, requestMockParams);
        try {
            return this.getReturn(compareMockConfig, apiDefinition.getId(), apiDefinition.getProjectId(), response);
        } catch (Exception e) {
            return this.requestNotFound(response);
        }
    }

    private ApiDefinition selectByProjectNumAndUrl(String projectNum, String method, String requestUrlSuffix) {
        List<ApiDefinition> apiDefinitionList = extApiDefinitionMapper.selectByProjectNum(projectNum);

        ApiDefinition apiDefinition = null;
        for (ApiDefinition checkDefinition : apiDefinitionList) {
            if (StringUtils.equalsIgnoreCase(method, checkDefinition.getMethod()) && MockServerUtils.checkUrlMatch(checkDefinition.getPath(), requestUrlSuffix)) {
                apiDefinition = checkDefinition;
                break;
            }
        }
        return apiDefinition;
    }

    private ApiDefinitionMockConfig match(String apiId, Map<String, String> requestHeaderMap, HttpRequestParam requestMockParams) {
        ApiDefinitionMockConfig compareMockConfig = null;
        ApiDefinitionMockExample mockExample = new ApiDefinitionMockExample();
        mockExample.createCriteria().andApiDefinitionIdEqualTo(apiId).andEnableEqualTo(true);
        List<ApiDefinitionMock> apiDefinitionMockList = apiDefinitionMockMapper.selectByExample(mockExample);
        if (CollectionUtils.isNotEmpty(apiDefinitionMockList)) {
            ApiDefinitionMockConfigExample mockConfigExample = new ApiDefinitionMockConfigExample();
            mockConfigExample.createCriteria().andIdIn(apiDefinitionMockList.stream().map(ApiDefinitionMock::getId).toList());
            List<ApiDefinitionMockConfig> mockConfigs = apiDefinitionMockConfigMapper.selectByExampleWithBLOBs(mockConfigExample);
            for (ApiDefinitionMockConfig mockConfig : mockConfigs) {
                if (MockServerUtils.matchMockConfig(mockConfig.getMatching(), requestHeaderMap, requestMockParams)) {
                    compareMockConfig = mockConfig;
                    break;
                }
            }
        }
        return compareMockConfig;
    }

    private Object getReturn(ApiDefinitionMockConfig compareMockConfig, String apiId, String projectId, HttpServletResponse response) {
        ResponseBody responseBody = null;
        List<MsHeader> responseHeader = null;
        int responseCode = -1;
        String useApiResponseId = null;

        if (compareMockConfig != null) {
            MockResponse mockResponse = JSON.parseObject(new String(compareMockConfig.getResponse()), MockResponse.class);
            if (mockResponse.isUseApiResponse()) {
                useApiResponseId = mockResponse.getApiResponseId();
            } else {
                responseCode = mockResponse.getStatusCode();
                responseHeader = mockResponse.getHeaders();
                responseBody = mockResponse.getBody();
            }
        }
        if (StringUtils.isNotBlank(useApiResponseId) || responseCode == -1) {
            HttpResponse mockSelectResponse = null;
            ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(apiId);
            if (blob != null) {
                List<HttpResponse> responseList = JSON.parseArray(new String(blob.getResponse()), HttpResponse.class);
                HttpResponse defaultHttpResponse = null;
                for (HttpResponse httpResponse : responseList) {
                    if (httpResponse.isDefaultFlag()) {
                        defaultHttpResponse = httpResponse;
                    }
                    if (StringUtils.equals(httpResponse.getId(), useApiResponseId)) {
                        mockSelectResponse = httpResponse;
                        break;
                    }
                }
                if (mockSelectResponse == null) {
                    mockSelectResponse = defaultHttpResponse;
                }
            }
            if (mockSelectResponse == null) {
                return this.requestNotFound(response);
            } else {
                responseCode = Integer.parseInt(mockSelectResponse.getStatusCode());
                responseHeader = mockSelectResponse.getHeaders();
                responseBody = mockSelectResponse.getBody();
            }
        }

        //返回响应码
        response.setStatus(responseCode);
        if (CollectionUtils.isNotEmpty(responseHeader)) {
            responseHeader.forEach(header -> {
                if (header.getEnable()) {
                    response.addHeader(header.getKey(), header.getValue());
                }
            });
        }
        if (responseBody == null) {
            return StringUtils.EMPTY;
        } else {
            if (StringUtils.equalsIgnoreCase(responseBody.getBodyType(), Body.BodyType.JSON.name())) {
                return responseBody.getJsonBody().getJsonWithSchema();
            } else if (StringUtils.equalsIgnoreCase(responseBody.getBodyType(), Body.BodyType.XML.name())) {
                return responseBody.getXmlBody().getValue();
            } else if (StringUtils.equalsIgnoreCase(responseBody.getBodyType(), Body.BodyType.RAW.name())) {
                return responseBody.getRawBody().getValue();
            } else {
                String fileId = responseBody.getBinaryBody().getFile().getFileId();
                String fileName = responseBody.getBinaryBody().getFile().getFileName();
                String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
                MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
                if (responseBody.getBinaryBody().isSendAsBody()) {
                    String contentType = "text/plain";
                    if (TempFileUtils.isImage(fileType)) {
                        contentType = "image/" + fileType;
                    }
                    mediaType = MediaType.parseMediaType(contentType);
                }
                byte[] bytes = new byte[0];
                FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
                if (fileMetadata != null) {
                    try {
                        String filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), fileManagementService.getFile(fileMetadata));
                        bytes = TempFileUtils.getFile(filePath);
                    } catch (Exception ignore) {
                        return StringUtils.EMPTY;
                    }
                } else {
                    ApiFileResource apiFileResource = apiFileResourceMapper.selectByPrimaryKey(compareMockConfig.getId(), fileId);
                    if (apiFileResource != null) {
                        FileRepository defaultRepository = FileCenter.getDefaultRepository();
                        FileRequest fileRequest = new FileRequest();
                        fileRequest.setFileName(apiFileResource.getFileName());
                        fileRequest.setFolder(DefaultRepositoryDir.getApiDefinitionDir(projectId, compareMockConfig.getId()) + "/" + fileId);
                        try {
                            bytes = defaultRepository.getFile(fileRequest);
                        } catch (Exception ignore) {
                        }
                    }
                }

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(bytes);
            }
        }
    }

    private String requestNotFound(HttpServletResponse response) {
        response.setStatus(404);
        return Translator.get("mock_warning");
    }
}