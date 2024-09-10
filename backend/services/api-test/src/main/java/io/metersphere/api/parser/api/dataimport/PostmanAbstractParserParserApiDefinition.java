package io.metersphere.api.parser.api.dataimport;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.BodyParamType;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.dto.request.http.body.WWWFormKV;
import io.metersphere.api.parser.api.postman.PostmanItem;
import io.metersphere.api.parser.api.postman.PostmanKeyValue;
import io.metersphere.api.parser.api.postman.PostmanRequest;
import io.metersphere.api.parser.api.postman.PostmanResponse;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.environment.auth.BasicAuth;
import io.metersphere.project.dto.environment.auth.DigestAuth;
import io.metersphere.project.dto.environment.auth.HTTPAuthConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class PostmanAbstractParserParserApiDefinition<T> extends HttpApiDefinitionImportAbstractParser<T> {


    private static final String POSTMAN_AUTH_TYPE_BASIC = "basic";
    private static final String POSTMAN_AUTH_TYPE_DIGEST = "digest";

    private static final String QUERY = "query";
    private static final String PATH = "path";
    private static final String VARIABLE = "variable";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String RAW = "raw";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String DISABLED = "disabled";
    private static final String MODE = "mode";
    private static final String FORM_DATA = "formdata";
    private static final String URL_ENCODED = "urlencoded";
    private static final String FILE = "file";
    private static final String OPTIONS = "options";
    private static final String La = "language";
    private static final String JSON = "json";
    private static final String XML = "xml";

    private MsHTTPElement parseElement(PostmanRequest requestDesc, String name, String url, JsonNode urlNode) {
        MsHTTPElement request = buildHttpRequest(name, url, requestDesc.getMethod());
        //设置认证
        setAuth(requestDesc, request);
        //设置基础参数 请求头
        setHeader(requestDesc, request);
        if (urlNode instanceof ObjectNode urlObject) {
            //设置query参数
            setQuery(urlObject, request);
            //设置rest参数
            setRest(urlObject, request);
        }
        //设置body参数
        setBody(requestDesc, request);
        return request;
    }

    protected Map<ApiDefinitionDetail, List<ApiDefinitionDetail>> parsePostman(PostmanItem requestItem, String modulePath, ImportRequest importRequest) {
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        String url = getUrl(requestDesc.getUrl());
        ApiDefinitionDetail detail = buildApiDefinition(requestItem.getName(), url, requestDesc.getMethod(), modulePath, importRequest);
        MsHTTPElement request = parseElement(requestDesc, requestItem.getName(), url, requestDesc.getUrl());

        // 其他设置
        PostmanItem.ProtocolProfileBehavior protocolProfileBehavior = requestItem.getProtocolProfileBehavior();
        request.getOtherConfig().setFollowRedirects(protocolProfileBehavior != null &&
                BooleanUtils.isTrue(protocolProfileBehavior.getFollowRedirects()));
        request.getOtherConfig().setAutoRedirects(!request.getOtherConfig().getFollowRedirects());

        //构造 children
        LinkedList<AbstractMsTestElement> children = new LinkedList<>();
        children.add(new MsCommonElement());
        request.setChildren(children);
        detail.setRequest(request);

        List<ApiDefinitionDetail> postmanExamples = this.parsePostmanRequest(requestItem, modulePath, importRequest);
        return new HashMap<>() {{
            this.put(detail, postmanExamples);
        }};
    }

    private String initApiCaseName(String originalName, List<String> savedResponseName) {
        if (savedResponseName.contains(originalName)) {
            int i = 1;
            while (savedResponseName.contains(originalName + "_" + i)) {
                i++;
            }
            return originalName + "_" + i;
        }
        return originalName;
    }

    private List<ApiDefinitionDetail> parsePostmanRequest(PostmanItem requestItem, String modulePath, ImportRequest importRequest) {
        List<ApiDefinitionDetail> postmanExamples = new ArrayList<>();
        List<PostmanResponse> response = requestItem.getResponse();
        List<String> savedResponseName = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(response)) {
            response.forEach(r -> {
                PostmanRequest postmanRequest = r.getOriginalRequest();
                String name = this.initApiCaseName(r.getName(), savedResponseName);
                String url = getUrl(postmanRequest.getUrl());
                ApiDefinitionDetail detail = buildApiDefinition(name, url, postmanRequest.getMethod(), modulePath, importRequest);
                MsHTTPElement request = parseElement(postmanRequest, name, url, postmanRequest.getUrl());
                //构造 children
                LinkedList<AbstractMsTestElement> children = new LinkedList<>();
                children.add(new MsCommonElement());
                request.setChildren(children);
                detail.setRequest(request);
                postmanExamples.add(detail);
                savedResponseName.add(name);
            });
        }
        return postmanExamples;
    }

    @NotNull
    private static MsHeader getMsHeader(PostmanKeyValue h) {
        MsHeader msHeader = new MsHeader();
        msHeader.setKey(h.getKey());
        msHeader.setValue(h.getValue());
        msHeader.setDescription(h.getDescription() != null ? h.getDescription().asText() : StringUtils.EMPTY);
        msHeader.setEnable(BooleanUtils.isFalse(h.isDisabled()));
        return msHeader;
    }

    private static void setBody(PostmanRequest requestDesc, MsHTTPElement request) {
        JsonNode bodyNode = requestDesc.getBody();
        if (bodyNode != null) {
            JsonNode modeNode = bodyNode.get(MODE);
            if (modeNode != null) {
                switch (modeNode.asText()) {
                    case FORM_DATA:
                        setFormData(bodyNode, request);
                        break;
                    case URL_ENCODED:
                        setWwwData(bodyNode, request);
                        break;
                    case RAW:
                        setRaw(bodyNode, request);
                        break;
                    case FILE:
                        setBinary(bodyNode, request);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void setRaw(JsonNode bodyNode, MsHTTPElement request) {
        JsonNode rawNode = bodyNode.get(RAW);
        JsonNode optionNode = bodyNode.get(OPTIONS);
        if (ObjectUtils.allNotNull(rawNode, optionNode)) {
            if (optionNode instanceof ObjectNode optionObject) {
                JsonNode languageNode = optionObject.get(RAW).get(La);
                if (languageNode instanceof TextNode languageText) {
                    if (StringUtils.equals(languageText.asText(), JSON)) {
                        request.getBody().setBodyType(Body.BodyType.JSON.name());
                        request.getBody().getJsonBody().setJsonValue(rawNode instanceof TextNode ? rawNode.asText() : StringUtils.EMPTY);
                    } else if (StringUtils.equals(languageText.asText(), XML)) {
                        request.getBody().setBodyType(Body.BodyType.XML.name());
                        request.getBody().getXmlBody().setValue(rawNode instanceof TextNode ? rawNode.asText() : StringUtils.EMPTY);
                    } else {
                        request.getBody().setBodyType(Body.BodyType.RAW.name());
                        request.getBody().getRawBody().setValue(rawNode instanceof TextNode ? rawNode.asText() : StringUtils.EMPTY);
                    }
                }
            }
        }
    }

    private static void setBinary(JsonNode bodyNode, MsHTTPElement request) {
        JsonNode fileNode = bodyNode.get(FILE);
        if (fileNode != null) {
            request.getBody().getBinaryBody().setFile(new ApiFile());
        }
        request.getBody().setBodyType(Body.BodyType.BINARY.name());
    }

    private static void setWwwData(JsonNode bodyNode, MsHTTPElement request) {
        JsonNode modeNode = bodyNode.get(URL_ENCODED);
        if (modeNode instanceof ArrayNode urlEncodedArray) {
            urlEncodedArray.forEach(u -> {
                WWWFormKV wwwFormKV = new WWWFormKV();
                wwwFormKV.setKey(u.get(KEY).asText());
                wwwFormKV.setValue(u.get(VALUE).asText());
                wwwFormKV.setDescription(u.get(DESCRIPTION) instanceof TextNode ? u.get(DESCRIPTION).asText() : StringUtils.EMPTY);
                wwwFormKV.setEnable(!(u.get(DISABLED) instanceof BooleanNode) || !u.get(DISABLED).asBoolean());
                request.getBody().getWwwFormBody().getFormValues().add(wwwFormKV);
            });
        }
        request.getBody().setBodyType(Body.BodyType.WWW_FORM.name());
    }

    private static void setFormData(JsonNode bodyNode, MsHTTPElement request) {
        JsonNode modeNode = bodyNode.get(FORM_DATA);
        if (modeNode instanceof ArrayNode formArray) {
            formArray.forEach(f -> {
                FormDataKV formDataKV = new FormDataKV();
                formDataKV.setKey(f.get(KEY).asText());
                String type = f.get(TYPE).asText();
                if (StringUtils.equals(type, FILE)) {
                    formDataKV.setParamType(BodyParamType.FILE.getValue());
                    formDataKV.setFiles(new ArrayList<>());
                } else {
                    formDataKV.setParamType(BodyParamType.STRING.getValue());
                    formDataKV.setValue(f.get(VALUE).asText());
                }
                formDataKV.setDescription(f.get(DESCRIPTION) instanceof TextNode ? f.get(DESCRIPTION).asText() : StringUtils.EMPTY);
                formDataKV.setEnable(!(f.get(DISABLED) instanceof BooleanNode) || !f.get(DISABLED).asBoolean());
                request.getBody().getFormDataBody().getFormValues().add(formDataKV);
            });
        }
        request.getBody().setBodyType(Body.BodyType.FORM_DATA.name());
    }

    private static void setRest(JsonNode urlObject, MsHTTPElement request) {
        JsonNode restNode = urlObject.get(VARIABLE);
        if (restNode instanceof ArrayNode restArray) {
            restArray.forEach(r -> {
                RestParam restParam = new RestParam();
                restParam.setKey(r.get(KEY) != null ? r.get(KEY).asText() : StringUtils.EMPTY);
                restParam.setValue(r.get(VALUE) != null ? r.get(VALUE).asText() : StringUtils.EMPTY);
                if (r.get(DESCRIPTION) != null) {
                    restParam.setDescription(r.get(DESCRIPTION) instanceof TextNode ? r.get(DESCRIPTION).asText() : StringUtils.EMPTY);
                }
                if (r.get(DESCRIPTION) != null) {
                    restParam.setEnable(!(r.get(DISABLED) instanceof BooleanNode) || !r.get(DISABLED).asBoolean());
                }
                request.getRest().add(restParam);
            });
        }
    }

    private static void setQuery(JsonNode urlObject, MsHTTPElement request) {
        JsonNode queryNode = urlObject.get(QUERY);
        if (queryNode instanceof ArrayNode queryArray) {
            queryArray.forEach(q -> {
                QueryParam queryParam = new QueryParam();
                queryParam.setKey(q.get(KEY).asText());
                queryParam.setValue(q.get(VALUE).asText());
                queryParam.setDescription(q.get(DESCRIPTION) instanceof TextNode ? q.get(DESCRIPTION).asText() : StringUtils.EMPTY);
                queryParam.setEnable(!(q.get(DISABLED) instanceof BooleanNode) || !q.get(DISABLED).asBoolean());
                request.getQuery().add(queryParam);
            });
        }
    }

    private static void setHeader(PostmanRequest requestDesc, MsHTTPElement request) {
        if (CollectionUtils.isNotEmpty(requestDesc.getHeader())) {
            requestDesc.getHeader().forEach(h -> {
                MsHeader msHeader = getMsHeader(h);
                request.getHeaders().add(msHeader);
            });
        }
    }

    private static String getUrl(JsonNode urlNode) {
        String url = StringUtils.EMPTY;
        if (urlNode instanceof ObjectNode urlObject) {
            JsonNode pathNode = urlObject.get(PATH);
            if (pathNode instanceof ArrayNode pathArray) {
                StringBuilder path = new StringBuilder();
                pathArray.forEach(p -> {
                    if (p instanceof TextNode pathString) {
                        if (pathString.asText().startsWith(":")) {
                            path.append(StringUtils.join("/{", pathString.asText().substring(1), "}"));
                        } else {
                            path.append(StringUtils.join("/", pathString.asText()));
                        }
                    }
                });
                url = path.toString();
            }
        } else if (urlNode instanceof TextNode urlText) {
            url = urlText.asText();
        }
        return url;
    }


    private static void setAuth(PostmanRequest requestDesc, MsHTTPElement request) {
        JsonNode auth = requestDesc.getAuth();
        if (auth != null) {
            JsonNode authNode = auth.get(TYPE);
            HTTPAuthConfig authConfig = request.getAuthConfig();
            if (authNode != null && StringUtils.equals(authNode.asText(), POSTMAN_AUTH_TYPE_BASIC)) {
                BasicAuth basicAuth = new BasicAuth();
                authConfig.setAuthType(HTTPAuthConfig.HTTPAuthType.BASIC.name());
                DigestAuth digestAuth = buildAuth(POSTMAN_AUTH_TYPE_BASIC, auth);
                basicAuth.setUserName(digestAuth.getUserName());
                basicAuth.setPassword(digestAuth.getPassword());
                authConfig.setBasicAuth(basicAuth);
            } else if (authNode != null && StringUtils.equals(authNode.asText(), POSTMAN_AUTH_TYPE_DIGEST)) {
                DigestAuth digestAuth = buildAuth(POSTMAN_AUTH_TYPE_DIGEST, auth);
                authConfig.setAuthType(HTTPAuthConfig.HTTPAuthType.DIGEST.name());
                authConfig.setDigestAuth(digestAuth);
            }
            request.setAuthConfig(authConfig);
        }
    }

    private static DigestAuth buildAuth(String type, JsonNode authNode) {
        DigestAuth digestAuth = new DigestAuth();
        JsonNode digestNode = authNode.get(type);
        if (digestNode != null) {
            if (digestNode instanceof ObjectNode digestObject) {
                JsonNode usernameNode = digestObject.get(USERNAME);
                JsonNode passwordNode = digestObject.get(PASSWORD);
                digestAuth.setUserName(usernameNode instanceof TextNode ? usernameNode.asText() : StringUtils.EMPTY);
                digestAuth.setPassword(passwordNode instanceof TextNode ? passwordNode.asText() : StringUtils.EMPTY);
            } else if (digestNode instanceof ArrayNode digestArray) {
                digestArray.forEach(node -> {
                    JsonNode keyNode = node.get(KEY);
                    JsonNode valueNode = node.get(VALUE);
                    if (StringUtils.equals(keyNode.asText(), USERNAME)) {
                        digestAuth.setUserName(valueNode.asText());
                    } else if (StringUtils.equals(keyNode.asText(), PASSWORD)) {
                        digestAuth.setPassword(valueNode.asText());
                    }
                });
            }
        }
        return digestAuth;
    }


}
