package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class PostmanAbstractParserParser<T> extends ApiImportAbstractParser<T> {

    protected MsHTTPSamplerProxy parsePostman(PostmanItem requestItem) {
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        requestDesc.getAuth(); // todo 认证方式等待优化
        PostmanUrl url = requestDesc.getUrl();
        MsHTTPSamplerProxy request = buildRequest(requestItem.getName(), url.getRaw(), requestDesc.getMethod());
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = parseVariable(path);
            request.setPath(path);
        } else {
            request.setPath("/");
        }
        parseBody(request.getBody(), requestDesc);
        request.setArguments(parseKeyValue(url.getQuery()));
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        addBodyHeader(request);
        addPreScript(request, requestItem.getEvent());
        return request;
    }


    /**
     *  将postman的变量转换成ms变量
     *  {{xxx}} -> ${xxx}
     * @param value
     * @return
     */
    public String parseVariable(String value) {
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

    private void addPreScript(MsHTTPSamplerProxy request, List<PostmanEvent> event) {
        if (request != null && CollectionUtils.isNotEmpty(event)) {
            StringBuilder scriptStr = new StringBuilder();
            event = event.stream()
                    .filter(item -> item.getScript() != null)
                    .collect(Collectors.toList());
            event.forEach(item -> {
                PostmanScript script = item.getScript();
                if (script != null && item.getListen().contains("prerequest")) {
                    List<String> exec = script.getExec();
                    if (CollectionUtils.isNotEmpty(exec)) {
                        exec.forEach(col -> {
                            if (StringUtils.isNotEmpty(col)) {
                                scriptStr.append(col + "\n");
                            }
                        });
                    }
                }
            });
            if (StringUtils.isNotBlank(scriptStr)) {
                MsJSR223PreProcessor jsr223PreProcessor = new MsJSR223PreProcessor();
                jsr223PreProcessor.setName("JSR223PreProcessor");
                jsr223PreProcessor.setScriptLanguage("nashornScript");
                jsr223PreProcessor.setScript(parseVariable(scriptStr.toString()));
                LinkedList<MsTestElement> hashTree = new LinkedList<>();
                hashTree.add(jsr223PreProcessor);
                request.setHashTree(hashTree);
            }
        }
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> {
            String k = parseVariable(item.getKey());
            String v = parseVariable(item.getValue());
            String desc = parseVariable(item.getDescription());
            keyValues.add(new KeyValue(k, v, desc, item.getContentType()));
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
                        bodyType = Body.JSON;
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
