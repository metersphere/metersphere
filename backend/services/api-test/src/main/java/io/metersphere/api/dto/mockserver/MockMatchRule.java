package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.request.http.body.Body;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mock匹配规则
 */
@Data
public class MockMatchRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求头匹配规则")
    private KeyValueMatchRule header = new KeyValueMatchRule();

    @Schema(description = "query参数匹配规则")
    private KeyValueMatchRule query = new KeyValueMatchRule();

    @Schema(description = "REST参数匹配规则")
    private KeyValueMatchRule rest = new KeyValueMatchRule();

    @Schema(description = "body参数匹配规则")
    private BodyParamMatchRule body = new BodyParamMatchRule();

    public boolean keyValueMatch(String matchType, Map<String, String> matchParam) {
        KeyValueMatchRule matchRule = switch (matchType) {
            case "header" -> header;
            case "query" -> query;
            case "rest" -> rest;
            default -> null;
        };
        if (matchRule == null) {
            return true;
        }
        return matchRule.match(matchParam, StringUtils.equals("header", matchType));
    }

    public boolean requestParamMatch(HttpRequestParam httpRequestParam) {
        if (!this.keyValueMatch("rest", httpRequestParam.getRestParams())) {
            return false;
        }
        if (!this.keyValueMatch("query", httpRequestParam.getQueryParamsObj())) {
            return false;
        }
        if (httpRequestParam.isPost()) {
            switch (Body.BodyType.valueOf(body.getBodyType())) {
                case XML:
                    return body.matchXml(httpRequestParam.getXmlToJsonParam());
                case JSON:
                    return body.matchJson(httpRequestParam.getJsonString());
                case FORM_DATA:
                    MockFormDataBody formDataBody = body.getFormDataBody();
                    KeyValueMatchRule formDataBodyRule = new KeyValueMatchRule();
                    if (formDataBody != null) {
                        formDataBodyRule.setMatchAll(formDataBody.isMatchAll());
                        List<KeyValueInfo> matchRules = new ArrayList<>();
                        formDataBodyRule.setMatchRules(matchRules);
                        formDataBody.getMatchRules().stream()
                                .filter(keyValueInfo -> StringUtils.isNotBlank(keyValueInfo.getKey()))
                                .forEach(keyValueInfo -> {
                                    if (CollectionUtils.isNotEmpty(keyValueInfo.getFiles())) {
                                        keyValueInfo.setValue(
                                                keyValueInfo.getFiles().stream()
                                                        .map(ApiFile::getFileName)
                                                        .toList()
                                                        .toString()
                                        );
                                    }
                                    formDataBodyRule.getMatchRules().add(keyValueInfo);
                                });
                    }
                    return formDataBodyRule.match(httpRequestParam.getBodyParamsObj(), false);
                case RAW:
                    return StringUtils.contains(body.getRawBody().getValue(), httpRequestParam.getRaw());
                case WWW_FORM:
                    return body.getWwwFormBody().match(httpRequestParam.getBodyParamsObj(), false);
                default:
                    return true;
            }
        }
        return true;
    }
}