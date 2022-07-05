package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.dto.parse.postman.PostmanKeyValue;
import io.metersphere.api.parse.PostmanAbstractParserParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.ProjectApplicationService;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class PostmanDefinitionParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        JSONObject jsonObject = JSON.parseObject(testStr);
        Object info = jsonObject.get("info");
        if (info == null) {
            MSException.throwException("wrong format");
        } else {
            JSONObject jsonObject1 = JSON.parseObject(info.toString());
            if (jsonObject1.get("_postman_id") == null) {
                MSException.throwException("wrong format");
            }
        }
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class, Feature.DisableSpecialKeyDetect);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        String modulePath = null;
        if (StringUtils.isNotBlank(postmanCollection.getInfo().getName())) {
            modulePath = "/" + postmanCollection.getInfo().getName();
        }
        List<ApiTestCaseWithBLOBs> cases = new ArrayList<>();
        Map<String, String> repeatMap = new HashMap();
        ProjectMapper projectMapper = CommonBeanFactory.getBean(ProjectMapper.class);
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        ProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
        boolean urlRepeat = config.getUrlRepeatable();
        parseItem(postmanCollection.getItem(), modulePath, variables, results,
                cases, repeatMap, urlRepeat);
        Collections.reverse(results); // 调整顺序
        Collections.reverse(cases);
        apiImport.setData(results);
        apiImport.setCases(cases);
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, String modulePath, List<PostmanKeyValue> variables, List<ApiDefinitionWithBLOBs> results,
                             List<ApiTestCaseWithBLOBs> cases, Map<String, String> repeatMap, Boolean repeatable) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {

                if (StringUtils.isNotBlank(modulePath) && StringUtils.isNotBlank(item.getName())) {
                    modulePath = modulePath + "/" + item.getName();
                }
                parseItem(childItems, modulePath, variables, results, cases, repeatMap, repeatable);
            } else {
                MsHTTPSamplerProxy msHTTPSamplerProxy = parsePostman(item);
                HttpResponse response = parsePostmanResponse(item);
                ApiDefinitionWithBLOBs request = buildApiDefinition(msHTTPSamplerProxy.getId(), msHTTPSamplerProxy.getName(),
                        msHTTPSamplerProxy.getPath(), msHTTPSamplerProxy.getMethod(), new ApiTestImportRequest());
                request.setPath(msHTTPSamplerProxy.getPath());
                request.setRequest(JSON.toJSONString(msHTTPSamplerProxy));
                request.setResponse(JSON.toJSONString(response));

                if (StringUtils.isNotBlank(modulePath)) {
                    request.setModulePath(modulePath);
                }
                if (request != null) {
                    if (repeatMap.keySet().contains(request.getMethod() + request.getPath())
                            && (repeatable == null || repeatable == false)) {
                        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                        BeanUtils.copyBean(apiTestCase, request);
                        apiTestCase.setApiDefinitionId(repeatMap.get(request.getMethod() + request.getPath()));
                        apiTestCase.setPriority("P0");
                        cases.add(apiTestCase);
                    } else {
                        repeatMap.put(request.getMethod() + request.getPath(), request.getId());
                        results.add(request);
                    }
                }
            }
        }
    }
}
