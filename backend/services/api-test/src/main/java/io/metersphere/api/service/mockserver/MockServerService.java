package io.metersphere.api.service.mockserver;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.mockserver.BodyParamMatchRule;
import io.metersphere.api.dto.mockserver.HttpRequestParam;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.body.BinaryBody;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileManagementService fileManagementService;

    private boolean isUrlParamMethod(String method) {
        return StringUtils.equalsAnyIgnoreCase(method,
                HttpMethodConstants.GET.name(),
                HttpMethodConstants.DELETE.name(),
                HttpMethodConstants.OPTIONS.name(),
                HttpMethodConstants.HEAD.name());
    }


    public ResponseEntity<?> execute(String method, String projectNum, String apiNum, HttpServletRequest request) {
        var requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        String url = request.getRequestURL().toString();
        String requestUrlSuffix = MockServerUtils.getUrlSuffix(StringUtils.joinWith("/", "/mock-server", projectNum, apiNum), request);

        // Try to find API definition based on projectNum and apiNum
        ApiDefinition apiDefinition = extApiDefinitionMapper.selectByProjectNumAndApiNum(projectNum, apiNum);
        if (apiDefinition == null) {
            // If not found, try to find API definition based on projectNum and requestUrlSuffix
            requestUrlSuffix = MockServerUtils.getUrlSuffix(StringUtils.joinWith("/", "/mock-server", projectNum), request);
            apiDefinition = selectByProjectNumAndUrl(projectNum, method, requestUrlSuffix);
        }

        // If API definition is still null, return not found
        if (apiDefinition == null) {
            return requestNotFound();
        }

        // Check if method and path match the API definition
        if (!StringUtils.equalsIgnoreCase(method, apiDefinition.getMethod()) || !MockServerUtils.checkUrlMatch(apiDefinition.getPath(), requestUrlSuffix)) {
            return requestNotFound();
        }

        // Get request mock params and find matching mock config
        HttpRequestParam requestMockParams = MockServerUtils.getHttpRequestParam(request, requestUrlSuffix, apiDefinition.getPath(), !isUrlParamMethod(method));
        LogUtils.info("Mock [" + url + "] Header:{}", requestHeaderMap);
        LogUtils.info("Mock [" + url + "] request:{}", JSON.toJSONString(requestMockParams));
        ApiDefinitionMockConfig compareMockConfig = findMatchingMockConfig(apiDefinition.getId(), requestHeaderMap, requestMockParams);

        // Get and return the response body
        try {

            return getResponseBody(compareMockConfig, apiDefinition.getId(), apiDefinition.getProjectId());
        } catch (Exception e) {
            return requestNotFound();
        }
    }

    private ApiDefinition selectByProjectNumAndUrl(String projectNum, String method, String requestUrlSuffix) {
        return extApiDefinitionMapper.selectByProjectNum(projectNum)
                .stream()
                .filter(checkDefinition -> StringUtils.equalsIgnoreCase(method, checkDefinition.getMethod()) && MockServerUtils.checkUrlMatch(checkDefinition.getPath(), requestUrlSuffix))
                .findFirst()
                .orElse(null);
    }

    private ApiDefinitionMockConfig findMatchingMockConfig(String apiId, Map<String, String> requestHeaderMap, HttpRequestParam param) {
        // 查询符合条件的 ApiDefinitionMockConfig 列表
        ApiDefinitionMockExample mockExample = new ApiDefinitionMockExample();
        mockExample.createCriteria().andApiDefinitionIdEqualTo(apiId).andEnableEqualTo(true);
        List<ApiDefinitionMock> apiDefinitionMockList = apiDefinitionMockMapper.selectByExample(mockExample);

        if (CollectionUtils.isEmpty(apiDefinitionMockList)) {
            return null;
        }

        ApiDefinitionMockConfigExample mockConfigExample = new ApiDefinitionMockConfigExample();
        mockConfigExample.createCriteria().andIdIn(apiDefinitionMockList.stream().map(ApiDefinitionMock::getId).collect(Collectors.toList()));
        List<ApiDefinitionMockConfig> mockConfigs = apiDefinitionMockConfigMapper.selectByExampleWithBLOBs(mockConfigExample);
        // 寻找匹配的 ApiDefinitionMockConfig
        return mockConfigs.stream()
                .filter(mockConfig -> MockServerUtils.matchMockConfig(mockConfig.getMatching(), requestHeaderMap, param) && matchBinaryBody(mockConfig, param.getBinaryParamsObj(), apiDefinitionMockList.getFirst().getProjectId()))
                .findFirst()
                .orElse(null);
    }

    private boolean matchBinaryBody(ApiDefinitionMockConfig mockConfig, byte[] binaryFile, String projectId) {
        MockMatchRule matchRule = JSON.parseObject(new String(mockConfig.getMatching()), MockMatchRule.class);
        BodyParamMatchRule bodyParamMatchRule = matchRule.getBody();
        if (bodyParamMatchRule != null && StringUtils.equals(bodyParamMatchRule.getBodyType(), Body.BodyType.BINARY.name())) {
            BinaryBody binaryBody = bodyParamMatchRule.getBinaryBody();
            if (binaryBody != null && binaryBody.getFile() != null) {
                ApiFile file = binaryBody.getFile();
                byte[] bytes = new byte[0];
                FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(file.getFileId());
                if (fileMetadata != null) {
                    try {
                        String filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), fileManagementService.getFile(fileMetadata));
                        bytes = TempFileUtils.getFile(filePath);
                    } catch (Exception ignore) {
                    }
                } else {
                    ApiFileResource apiFileResource = apiFileResourceMapper.selectByPrimaryKey(mockConfig.getId(), file.getFileId());
                    if (apiFileResource != null) {
                        FileRepository defaultRepository = FileCenter.getDefaultRepository();
                        FileRequest fileRequest = new FileRequest();
                        fileRequest.setFileName(apiFileResource.getFileName());
                        fileRequest.setFolder(DefaultRepositoryDir.getApiMockDir(projectId, mockConfig.getId()) + "/" + file.getFileId());
                        try {
                            bytes = defaultRepository.getFile(fileRequest);
                        } catch (Exception ignore) {
                        }
                    }
                }
                return Arrays.equals(bytes, binaryFile);
            }
        }
        return true;
    }

    private ResponseEntity<?> getResponseBody(ApiDefinitionMockConfig config, String apiId, String projectId) {
        ResponseBody responseBody = null;
        List<MsHeader> responseHeader = null;
        int responseCode = -1;
        String useApiResponseId = null;

        Long delay = null;
        if (config != null) {
            MockResponse mockResponse = JSON.parseObject(new String(config.getResponse()), MockResponse.class);
            // mock 响应引用的是接口自身响应内容
            if (mockResponse.isUseApiResponse()) {
                useApiResponseId = mockResponse.getApiResponseId();
            } else {
                responseCode = mockResponse.getStatusCode();
                responseHeader = mockResponse.getHeaders();
                responseBody = mockResponse.getBody();
                delay = mockResponse.getDelay();
            }
        }

        // 获取接口自身的响应作为mock
        if (StringUtils.isNotBlank(useApiResponseId) || responseCode == -1) {
            HttpResponse mockSelectResponse = null;
            ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(apiId);
            if (blob != null) {
                List<HttpResponse> responseList = JSON.parseArray(new String(blob.getResponse()), HttpResponse.class);
                HttpResponse defaultHttpResponse = responseList.stream()
                        .filter(HttpResponse::isDefaultFlag)
                        .findFirst()
                        .orElse(null);

                final String useId = useApiResponseId;
                mockSelectResponse = responseList.stream()
                        .filter(responseItem -> StringUtils.equals(responseItem.getId(), useId))
                        .findFirst()
                        .orElse(defaultHttpResponse);
            }

            if (mockSelectResponse != null) {
                responseCode = Integer.parseInt(mockSelectResponse.getStatusCode());
                responseHeader = mockSelectResponse.getHeaders();
                responseBody = mockSelectResponse.getBody();
            }
        }

        HttpHeaders headers = new HttpHeaders();
        if (CollectionUtils.isNotEmpty(responseHeader)) {
            responseHeader.stream()
                    .filter(MsHeader::getEnable)
                    .forEach(header -> headers.add(header.getKey(), header.getValue()));
        }

        if (responseBody != null) {
            if (delay != null && delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (Exception ignored) {
                }
            }
            boolean isMock = config != null;
            String resourceId = config != null ? config.getId() : apiId;
            return switch (responseBody.getBodyType()) {
                case "JSON" -> responseEntity(responseCode, responseBody.getJsonBody().getJsonValue(), headers);
                case "XML" -> responseEntity(responseCode, responseBody.getXmlBody().getValue(), headers);
                case "RAW" -> responseEntity(responseCode, responseBody.getRawBody().getValue(), headers);
                case "BINARY" -> handleBinaryBody(responseCode, responseBody, projectId, resourceId, isMock);
                default -> responseEntity(responseCode, StringUtils.EMPTY, headers);
            };
        }

        return requestNotFound();
    }

    private ResponseEntity<?> handleBinaryBody(int responseCode, ResponseBody responseBody, String projectId, String resourceId, boolean isMock) {
        String fileId = responseBody.getBinaryBody().getFile().getFileId();
        String fileName = responseBody.getBinaryBody().getFile().getFileName();
        String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (responseBody.getBinaryBody().isSendAsBody()) {
            String contentType = MediaType.TEXT_PLAIN_VALUE;
            if (TempFileUtils.isImage(fileType)) {
                contentType = "image/" + fileType;
                if (StringUtils.equalsIgnoreCase(fileType, "pdf")) {
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                }
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
                return requestNotFound();
            }
        } else {
            ApiFileResource apiFileResource = apiFileResourceMapper.selectByPrimaryKey(resourceId, fileId);
            if (apiFileResource != null) {
                FileRepository defaultRepository = FileCenter.getDefaultRepository();
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(apiFileResource.getFileName());
                String folder = isMock ? DefaultRepositoryDir.getApiMockDir(projectId, resourceId) : DefaultRepositoryDir.getApiDefinitionDir(projectId, resourceId);
                fileRequest.setFolder(folder + "/" + fileId);
                try {
                    bytes = defaultRepository.getFile(fileRequest);
                } catch (Exception ignore) {
                }
            }
        }

        return ResponseEntity.status(responseCode)
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    private ResponseEntity<?> requestNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Translator.get("mock_warning"));
    }

    private ResponseEntity<?> responseEntity(int code, String body, HttpHeaders headers) {
        return ResponseEntity.status(code).headers(headers).body(body);
    }

}