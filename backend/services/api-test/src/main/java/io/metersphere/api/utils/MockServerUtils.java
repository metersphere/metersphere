package io.metersphere.api.utils;

import io.metersphere.api.dto.mockserver.HttpRequestParam;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MockServerUtils {

    public static Map<String, String> getHttpRequestHeader(HttpServletRequest request) {
        Map<String, String> returnMap = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String headerValue = request.getHeader(header);
            returnMap.put(header, headerValue);
        }
        return returnMap;
    }

    public static HttpRequestParam getHttpRequestParam(HttpServletRequest request, String requestUrlSuffix, String apiDefinitionPath, boolean isPost) {
        HttpRequestParam requestParam = new HttpRequestParam();
        requestParam.setPost(isPost);

        LinkedHashMap<String, String> restParamMap = MockServerUtils.getRestParam(apiDefinitionPath, requestUrlSuffix);
        requestParam.setRestParams(restParamMap);

        //解析k-v参数
        LinkedHashMap<String, String> queryParamsMap = new LinkedHashMap<>();

        String requestPostString = null;

        try {
            if (request instanceof HttpServletRequestWrapper requestWrapper) {
                request = (HttpServletRequest) requestWrapper.getRequest();
            }
            if (request instanceof ShiroHttpServletRequest shiroHttpServletRequest) {
                String contentType = StringUtils.isBlank(request.getContentType()) ? StringUtils.EMPTY : request.getContentType();
                List<String> contentTypeLists = List.of(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE);
                if (StringUtils.equals(contentType, MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
                    InputStream inputStream = shiroHttpServletRequest.getRequest().getInputStream();
                    byte[] binaryParams = inputStream.readAllBytes();
                    requestParam.setBinaryParamsObj(binaryParams);
                } else if (contentTypeLists.contains(contentType)) {
                    String inputLine;
                    StringBuilder receiveData = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(
                            request.getInputStream(), StandardCharsets.UTF_8))) {
                        while ((inputLine = in.readLine()) != null) {
                            receiveData.append(inputLine);
                        }
                    }
                    requestPostString = receiveData.toString();
                } else if (StringUtils.equals(contentType, MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                    Map<String, String[]> parameterMap = request.getParameterMap();
                    LinkedHashMap<String, String> bodyParams = new LinkedHashMap<>();
                    if (parameterMap != null && !parameterMap.isEmpty()) {
                        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                            String key = entry.getKey();
                            String[] value = entry.getValue();
                            if (value != null && value.length > 0) {
                                String valueStr = value[0];
                                if (StringUtils.isNotEmpty(valueStr)) {
                                    bodyParams.put(key, valueStr);
                                }
                            }
                        }
                        requestParam.setBodyParamsObj(bodyParams);
                    }
                }
            }
            String queryString = request.getQueryString();

            if (StringUtils.isNotEmpty(queryString)) {
                String[] queryParamArr = queryString.split("&");
                for (String queryParam : queryParamArr) {
                    String[] queryParamKV = queryParam.split("=");
                    if (queryParamKV.length == 2) {
                        queryParamsMap.put(queryParamKV[0], URLDecoder.decode(queryParamKV[1], StandardCharsets.UTF_8));
                    }
                }
            }
            requestParam.setQueryParamsObj(queryParamsMap);
            if (request instanceof StandardMultipartHttpServletRequest standardMultipartHttpServletRequest) {
                LinkedHashMap<String, String> bodyParams = new LinkedHashMap<>();
                Collection<Part> parts = standardMultipartHttpServletRequest.getParts();
                for (Part part : parts) {
                    String name = part.getName();
                    String fileName = part.getSubmittedFileName();
                    String value = new BufferedReader(new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8)).readLine();

                    if (StringUtils.isBlank(fileName)) {
                        bodyParams.put(name, value);
                    }
                    if (StringUtils.isNotEmpty(fileName)) {
                        bodyParams.computeIfPresent(name, (key, currentValue) -> {
                            List<String> current = JSON.parseArray(currentValue, String.class);
                            current.add(fileName);
                            return current.toString();
                        });
                        if (!bodyParams.containsKey(name)) {
                            List<String> current = new ArrayList<>();
                            current.add(fileName);
                            bodyParams.put(name, current.toString());
                        }
                    }
                    requestParam.setBodyParamsObj(bodyParams);
                }
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
        }

        //解析body参数
        requestParam.setRaw(requestPostString);

        //解析paramType
        if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            requestParam.setJsonParam(requestPostString);
        } else if (StringUtils.endsWith(request.getContentType(), "/xml")) {
            requestParam.setXmlParam(requestPostString);
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            requestParam.setParamType(Body.BodyType.FORM_DATA.name());
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.TEXT_PLAIN_VALUE)) {
            requestParam.setParamType(Body.BodyType.RAW.name());
        } else if (isPost) {
            requestParam.setParamType(Body.BodyType.RAW.name());
        }
        return requestParam;
    }

    public static LinkedHashMap<String, String> getRestParam(String apiPath, String requestUrl) {
        LinkedHashMap<String, String> restParams = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(apiPath)) {
            if (apiPath.startsWith("/")) {
                apiPath = apiPath.substring(1);
            }
            if (requestUrl.startsWith("/")) {
                requestUrl = requestUrl.substring(1);
            }
            String[] pathArr = apiPath.split("/");
            String[] sendParamArr = requestUrl.split("/");

            //获取 url的<参数名-参数值>，通过匹配api的接口设置和实际发送的url
            for (int i = 0; i < pathArr.length; i++) {
                String param = pathArr[i];
                if (param.startsWith("{") && param.endsWith("}")) {
                    param = param.substring(1, param.length() - 1);
                    String value = StringUtils.EMPTY;
                    if (sendParamArr.length > i) {
                        value = sendParamArr[i];
                    }
                    restParams.put(param, URLDecoder.decode(value, StandardCharsets.UTF_8));
                }
            }
        }
        return restParams;
    }

    public static String getUrlSuffix(String mockUrlInfo, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String[] urlParamArr = requestUri.split(mockUrlInfo);
        return urlParamArr.length == 0 ? "" : urlParamArr[urlParamArr.length - 1];
    }

    public static boolean checkUrlMatch(String apiDefinitionPath, String requestUrlSuffix) {
        if (StringUtils.equalsAny(apiDefinitionPath, requestUrlSuffix, "/" + requestUrlSuffix)) {
            return true;
        } else {
            if (StringUtils.isNotEmpty(apiDefinitionPath)) {

                String urlSuffix = requestUrlSuffix;
                //去掉前缀的“/"
                if (urlSuffix.startsWith("/")) {
                    urlSuffix = urlSuffix.substring(1);
                }
                if (apiDefinitionPath.startsWith("/")) {
                    apiDefinitionPath = apiDefinitionPath.substring(1);
                }

                //如果请求后缀以"/"结尾，需要特殊处理
                boolean urlSuffixEndEmpty = false;
                if (urlSuffix.endsWith("/")) {
                    urlSuffixEndEmpty = true;
                    urlSuffix = urlSuffix + "emptyStrForSplit";
                }
                String[] requestUrlDomainArr = urlSuffix.split("/");
                if (urlSuffixEndEmpty) {
                    requestUrlDomainArr[requestUrlDomainArr.length - 1] = StringUtils.EMPTY;
                }
                urlSuffixEndEmpty = false;
                if (apiDefinitionPath.endsWith("/")) {
                    urlSuffixEndEmpty = true;
                    apiDefinitionPath = apiDefinitionPath + "emptyStrForSplit";
                }
                String[] apiPathDomainArr = apiDefinitionPath.split("/");
                if (urlSuffixEndEmpty) {
                    apiPathDomainArr[apiPathDomainArr.length - 1] = StringUtils.EMPTY;
                }

                if (apiPathDomainArr.length == requestUrlDomainArr.length) {
                    boolean isFetch = true;
                    for (int i = 0; i < requestUrlDomainArr.length; i++) {
                        String pathItem = apiPathDomainArr[i];
                        if (!(pathItem.startsWith("{") && pathItem.endsWith("}"))) {
                            if (!StringUtils.equals(apiPathDomainArr[i], requestUrlDomainArr[i])) {
                                return false;
                            }
                        }
                    }
                    return isFetch;
                }
            }
        }
        return false;
    }

    public static boolean matchMockConfig(byte[] mockMatchBytes, Map<String, String> requestHeaderMap, HttpRequestParam httpRequestParam) {
        try {
            MockMatchRule matchRule = JSON.parseObject(new String(mockMatchBytes), MockMatchRule.class);
            return matchRule.keyValueMatch("header", requestHeaderMap) && matchRule.requestParamMatch(httpRequestParam);
        } catch (Exception e) {
            LogUtils.info(e.getMessage());
        }
        return false;
    }
}
