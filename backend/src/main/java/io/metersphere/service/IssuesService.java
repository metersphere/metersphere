package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.RestTemplateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.IntegrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

@Service
public class IssuesService {

    @Resource
    private IntegrationService integrationService;


    public void testAuth() {
        String url = "https://api.tapd.cn/quickstart/testauth";
        ResultHolder call = call(url);
        System.out.println(call.getData());
    }

    public void addIssues() {
        String url = "https://api.tapd.cn/bugs";
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", "20200812bug2");
        paramMap.add("workspace_id", "55049933");
        paramMap.add("description", "20200812 api add bug 2");
        ResultHolder call = call(url, HttpMethod.POST, paramMap);
        System.out.println(call.getData());
    }

    private ResultHolder call(String url) {
        return call(url, HttpMethod.GET, null);
    }

    private ResultHolder call(String url, HttpMethod httpMethod, Object params) {
        String responseJson;

        String config = tapdConfig();
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");

        HttpHeaders header = tapdAuth(account, password);

        if (httpMethod.equals(HttpMethod.GET)) {
            responseJson = RestTemplateUtils.get(url, header);
        } else {
            responseJson = RestTemplateUtils.post(url, params, header);
        }

        ResultHolder result = JSON.parseObject(responseJson, ResultHolder.class);

        if (!result.isSuccess()) {
            MSException.throwException(result.getMessage());
        }
        return JSON.parseObject(responseJson, ResultHolder.class);

    }

    private String tapdConfig() {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
        request.setPlatform(IssuesManagePlatform.Tapd.toString());

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }

    private HttpHeaders tapdAuth(String apiUser, String password) {
        String authKey = EncryptUtils.base64Encoding(apiUser + ":" + password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        return headers;
    }
}
