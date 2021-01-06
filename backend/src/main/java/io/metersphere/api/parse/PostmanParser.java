package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanParser extends ApiImportAbstractParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionResult> results = new ArrayList<>();
        parseItem(postmanCollection.getItem(), variables, results, buildModule(getSelectModule(request.getModuleId()), postmanCollection.getInfo().getName(), request.isSaved()), request.isSaved());
        apiImport.setData(results);
        return apiImport;
    }

    private void parseItem(List<PostmanItem> items, List<PostmanKeyValue> variables, List<ApiDefinitionResult> results, ApiModule parentModule, boolean isSaved) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                ApiModule module = buildModule(parentModule, item.getName() , isSaved);
                parseItem(childItems, variables, results, module, isSaved);
            } else {
                ApiDefinitionResult request = parsePostman(item);
                if (request != null) {
                    results.add(request);
                }
                if (parentModule != null) {
                    request.setModuleId(parentModule.getId());
                }
            }
        }
    }

    private ApiDefinitionResult parsePostman(PostmanItem requestItem) {
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        requestDesc.getAuth(); // todo 认证方式等待优化
        PostmanUrl url = requestDesc.getUrl();
        MsHTTPSamplerProxy request = buildRequest(requestItem.getName(), url.getRaw(), requestDesc.getMethod());
        ApiDefinitionResult apiDefinition =
                buildApiDefinition(request.getId(), requestItem.getName(), url.getRaw(), requestDesc.getMethod());
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = path.replace("{{", "${");
            path = path.replace("}}", "}");
            request.setPath(path);
            apiDefinition.setPath(path);
        } else {
            request.setPath("/");
            apiDefinition.setPath("/");
        }
        parseBody(request.getBody(), requestDesc);
        request.setArguments(parseKeyValue(url.getQuery()));
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        apiDefinition.setRequest(JSON.toJSONString(request));
        return apiDefinition;
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new KeyValue(item.getKey(), item.getValue(), item.getDescription(), item.getContentType())));
        return keyValues;
    }

    private void parseBody(Body body, PostmanRequest requestDesc) {
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            parseRawBody(body, postmanBody, bodyMode);
        } else if (StringUtils.equalsAny(bodyMode, PostmanRequestBodyMode.FORM_DATA.value(), PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
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
        body.setRaw(postmanBody.getString(bodyMode));
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
