package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.request.http.body.Body;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;

//mock匹配规则
@Data
public class MockMatchRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "请求头匹配规则")
    private keyValueMatchRole header = new keyValueMatchRole();
    @Schema(description = "query参数匹配规则")
    private keyValueMatchRole query = new keyValueMatchRole();
    @Schema(description = "REST参数匹配规则")
    private keyValueMatchRole rest = new keyValueMatchRole();
    @Schema(description = "body参数匹配规则")
    private BodyParamMatchRole body = new BodyParamMatchRole();

    public boolean keyValueMatch(String matchType, Map<String, String> matchParam) {
        keyValueMatchRole matchRole = null;
        switch (matchType) {
            case "header":
                matchRole = header;
                break;
            case "query":
                matchRole = query;
                break;
            case "rest":
                matchRole = rest;
                break;
            case "body":
                if (body != null) {
                    matchRole = body.getFormDataMatch();
                }
                break;
            default:
                break;
        }
        if (matchRole == null) {
            return true;
        }
        return matchRole.match(matchParam);
    }

    public boolean requestParamMatch(HttpRequestParam httpRequestParam) {
        if (!this.keyValueMatch("rest", httpRequestParam.getRestParams())) {
            return false;
        }
        if (httpRequestParam.isPost()) {
            if (StringUtils.equalsIgnoreCase(body.getParamType(), Body.BodyType.XML.name())) {
                return body.matchXml(httpRequestParam.getXmlToJsonParam());
            } else if (StringUtils.equalsIgnoreCase(body.getParamType(), Body.BodyType.JSON.name())) {
                return body.matchJson(httpRequestParam.getJsonString());
            } else if (StringUtils.equalsIgnoreCase(body.getParamType(), Body.BodyType.FORM_DATA.name())) {
                return this.keyValueMatch("body", httpRequestParam.getQueryParamsObj());
            } else if (StringUtils.isNotBlank(body.getRaw())) {
                return StringUtils.contains(body.getRaw(), httpRequestParam.getRaw());
            }
        } else {
            return this.keyValueMatch("query", httpRequestParam.getQueryParamsObj());
        }
        return true;
    }
}