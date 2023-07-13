package io.metersphere.api.parse;

import com.fasterxml.jackson.databind.JsonNode;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class MsAbstractParser<T> extends ApiImportAbstractParser<T> {
    private static final String URL = "url";
    private static final String METHOD = "method";
    private static final String HEADERS = "headers";
    private static final String BODY = "body";

    protected List<MsHTTPSamplerProxy> parseMsHTTPSamplerProxy(JSONObject testObject, String tag, boolean isSetUrl) {
        JSONObject requests = testObject.optJSONObject(tag);
        List<MsHTTPSamplerProxy> msHTTPSamplerProxies = new ArrayList<>();
        if (requests != null) {
            requests.keySet().forEach(requestName -> {
                JSONObject requestObject = requests.optJSONObject(requestName);
                String path = requestObject.optString(URL);
                String method = requestObject.optString(METHOD);
                MsHTTPSamplerProxy request = buildRequest(requestName, path, method);
                parseBody(requestObject, request.getBody());
                parseHeader(requestObject, request.getHeaders());
                parsePath(request);
                if (isSetUrl) {
                    request.setUrl(path);
                }
                msHTTPSamplerProxies.add(request);
            });
        }
        return msHTTPSamplerProxies;
    }

    protected List<MsHTTPSamplerProxy> parseMsHTTPSamplerProxy(JsonNode requests, boolean isSetUrl) {
        List<MsHTTPSamplerProxy> samplerProxies = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = requests.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isNull()) {
                continue;
            }
            JSONObject requestObject = JSONUtil.parseObject(JSON.toJSONString(field.getValue()));
            String path = requestObject.optString(URL);
            String method = requestObject.optString(METHOD);
            MsHTTPSamplerProxy request = buildRequest(field.getKey(), path, method);
            parseBody(requestObject, request.getBody());
            parseHeader(requestObject, request.getHeaders());
            parsePath(request);
            if (isSetUrl) {
                request.setUrl(path);
            }
            samplerProxies.add(request);
        }
        return samplerProxies;
    }

    private void parsePath(MsHTTPSamplerProxy request) {
        if (StringUtils.isNotBlank(request.getPath())) {
            String[] split = request.getPath().split("\\?");
            String path = split[0];
            parseQueryParameters(split, request.getArguments());
            request.setPath(path);
        } else {
            request.setPath("/");
        }
    }

    private void parseQueryParameters(String[] split, List<KeyValue> arguments) {
        if (split.length > 1) {
            try {
                String queryParams = split[1];
                queryParams = URLDecoder.decode(queryParams, StandardCharsets.UTF_8.name());
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    arguments.add(new KeyValue(kv[0], kv.length < 2 ? null : kv[1]));
                }
            } catch (UnsupportedEncodingException e) {
                LogUtil.error(e);
            }
        }
    }

    private void parseHeader(JSONObject requestObject, List<KeyValue> msHeaders) {
        JSONArray headers = requestObject.optJSONArray(HEADERS);
        if (headers != null) {
            for (int i = 0; i < headers.length(); i++) {
                JSONObject header = headers.optJSONObject(i);
                msHeaders.add(new KeyValue(header.optString("name"), header.optString("value")));
            }
        }
    }

    private void parseBody(JSONObject requestObject, Body msBody) {
        if (requestObject.has(BODY)) {
            Object body = requestObject.get(BODY);
            if (body instanceof JSONArray) {
                JSONArray bodies = requestObject.optJSONArray(BODY);
                if (bodies != null) {
                    StringBuilder bodyStr = new StringBuilder();
                    for (int i = 0; i < bodies.length(); i++) {
                        String tmp = bodies.optString(i);
                        bodyStr.append(tmp);
                    }
                    msBody.setType(Body.RAW);
                    msBody.setRaw(bodyStr.toString());
                }
            } else if (body instanceof JSONObject) {
                JSONObject bodyObj = requestObject.optJSONObject(BODY);
                if (bodyObj != null) {
                    ArrayList<KeyValue> kvs = new ArrayList<>();
                    bodyObj.keySet().forEach(key -> {
                        kvs.add(new KeyValue(key, bodyObj.optString(key)));
                    });
                    msBody.setKvs(kvs);
                    msBody.setType(Body.WWW_FROM);
                }
            }
        }
    }
}
