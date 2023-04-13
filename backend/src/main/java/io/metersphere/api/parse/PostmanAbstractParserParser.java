package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PostmanAbstractParserParser<T> extends ApiImportAbstractParser<T> {

    protected MsHTTPSamplerProxy parsePostman(PostmanItem requestItem) {
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        requestDesc.getAuth(); // todo 认证方式等待优化
        PostmanUrl url = requestDesc.getUrl();
        MsHTTPSamplerProxy request = buildRequest(requestItem.getName(), url == null ? "" : url.getRaw(), requestDesc.getMethod(), Optional.ofNullable(requestDesc.getBody()).orElse(new JSONObject()).getString("jsonSchema"));
        request.setRest(parseKeyValue(url != null && CollectionUtils.isNotEmpty(url.getVariable()) ?
                url.getVariable() : new ArrayList<>()));
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = parseVariable(path);
            request.setPath(path);
        } else {
            request.setPath("/");
        }
        request.getBody().setType("KeyValue");
        parseBody(request.getBody(), requestDesc);
        request.setArguments(parseKeyValue(url == null ? new ArrayList<>() : url.getQuery()));
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        addBodyHeader(request);
        PostmanItem.ProtocolProfileBehavior protocolProfileBehavior = requestItem.getProtocolProfileBehavior();
        if (protocolProfileBehavior != null &&
                !protocolProfileBehavior.getFollowRedirects()) {
            request.setFollowRedirects(false);
        } else {
            request.setFollowRedirects(true);
        }
        request.setResponseTimeout("60000");
        request.setConnectTimeout("60000");
        return request;
    }

    protected HttpResponse parsePostmanResponse(PostmanItem requestItem) {
        List<PostmanResponse> requestList = requestItem.getResponse();
        if (CollectionUtils.isEmpty(requestList)) {
            return new HttpResponse();
        }
        PostmanResponse requestDesc = requestItem.getResponse().get(0);
        if (requestDesc == null) {
            return null;
        }
        PostmanUrl url = requestDesc.getOriginalRequest().getUrl();
        MsHTTPSamplerProxy request = buildRequest(requestItem.getName(), null, null, requestDesc.getJsonSchema());
        request.setRest(parseKeyValue(requestDesc.getOriginalRequest().getUrl().getVariable()));
        if (StringUtils.isNotBlank(requestDesc.getBody())) {
            request.getBody().setRaw(requestDesc.getBody());
        }
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = parseVariable(path);
            request.setPath(path);
        } else {
            request.setPath("/");
        }
        //todo response 后续支持更多类型导入
        request.getBody().setType(Body.JSON_STR);
        request.setArguments(parseKeyValue(url == null ? new ArrayList<>() : url.getQuery()));
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        addBodyHeader(request);
        HttpResponse response = new HttpResponse();
        BeanUtils.copyBean(response, request);
        response.setStatusCode(new ArrayList<>());
        response.setType("HTTP");
        return response;
    }


    /**
     * 将postman的变量转换成ms变量
     * {{xxx}} -> ${xxx}
     *
     * @param value
     * @return
     */
    public String parseVariable(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Pattern pattern = Pattern.compile("(\\{\\{(.*?)\\}\\})");
            Matcher matcher = pattern.matcher(value);
            while (matcher.find()) {
                value = matcher.replaceFirst("\\$\\{" + matcher.group(2) + "\\}");
                matcher = pattern.matcher(value);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return value;
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return new ArrayList<>();
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> {
            String k = parseVariable(item.getKey());
            String v = parseVariable(item.getValue());
            String desc = parseVariable(item.getDescription());
            KeyValue keyValue = new KeyValue(k, v, desc, item.getContentType());
            if (StringUtils.isNotBlank(item.getType()) && StringUtils.equals("file", item.getType())) {
                keyValue.setType("file");
            }
            keyValues.add(keyValue);
        });
        return keyValues;
    }

    private void parseBody(Body body, PostmanRequest requestDesc) {
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.isBlank(bodyMode)) {
            return;
        }
        if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            parseRawBody(body, postmanBody, bodyMode);
        } else if (StringUtils.equalsAny(bodyMode, PostmanRequestBodyMode.FORM_DATA.value(), PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(parseVariable(postmanBody.getString(bodyMode)), PostmanKeyValue.class);
            body.setKvs(parseKeyValue(postmanKeyValues));
            if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value())) {
                body.setType(Body.FORM_DATA);
            } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
                body.setType(Body.WWW_FROM);
            }
        } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FILE.value())) {
            body.setType(Body.BINARY);
            body.setKvs(new ArrayList<>());
        }
    }

    private void parseRawBody(Body body, JSONObject postmanBody, String bodyMode) {
        body.setRaw(parseVariable(postmanBody.getString(bodyMode)));
        body.setType(MsRequestBodyType.RAW.value());
        JSONObject options = postmanBody.getJSONObject("options");
        if (options != null) {
            JSONObject raw = options.getJSONObject(PostmanRequestBodyMode.RAW.value());
            if (raw != null) {
                String bodyType = "";
                switch (raw.getString("language")) {
                    case "json":
                        bodyType = Body.JSON_STR;
                        break;
                    case "xml":
                        bodyType = Body.XML;
                        break;
                    default:
                        bodyType = Body.RAW;
                }
                body.setType(bodyType);
            }
        }
    }
}
