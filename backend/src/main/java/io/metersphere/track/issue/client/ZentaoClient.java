package io.metersphere.track.issue.client;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.zentao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class ZentaoClient extends BaseClient {

    protected  String ENDPOINT;

    protected  String USER_NAME;

    protected  String PASSWD;

    public String login() {
        String sessionId = getSessionId();
        String url = getBaseUrl() + "/user-login.json?zentaosid=" + sessionId;
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("account", USER_NAME);
        paramMap.add("password", PASSWD);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        GetUserResponse getUserResponse = (GetUserResponse) getResultForObject(GetUserResponse.class, response);
        GetUserResponse.User user = getUserResponse.getUser();
        if (user == null) {
            LogUtil.error(JSONObject.toJSON(getUserResponse));
            // 登录失败，获取的session无效，置空session
            MSException.throwException("zentao login fail");
        }
        if (!StringUtils.equals(user.getAccount(), USER_NAME)) {
            LogUtil.error("login fail，inconsistent users");
            MSException.throwException("zentao login fail");
        }
        return sessionId;
    }

    public String getSessionId() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/api-getsessionid.json",
                HttpMethod.GET, null, String.class);
        GetSessionResponse getSessionResponse = (GetSessionResponse) getResultForObject(GetSessionResponse.class, response);
        return JSONObject.parseObject(getSessionResponse.getData(), GetSessionResponse.Session.class).getSessionID();
    }

    public AddIssueResponse.Issue addIssue(MultiValueMap<String, Object> paramMap) {
        String sessionId = login();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
           response = restTemplate.exchange(getBaseUrl() + "/api-getModel-bug-create.json?zentaosid=" + sessionId,
                    HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        AddIssueResponse addIssueResponse = (AddIssueResponse) getResultForObject(AddIssueResponse.class, response);
        return JSONObject.parseObject(addIssueResponse.getData(), AddIssueResponse.Issue.class);
    }

    public GetIssueResponse.Issue getBugById(String id) {
        String sessionId = login();
        String url = getBaseUrl() + "/api-getModel-bug-getById-bugID={1}?zentaosid={2}";
        ResponseEntity<String> response = restTemplate.exchange(url,
                HttpMethod.GET, null, String.class, id, sessionId);
        GetIssueResponse getIssueResponse = (GetIssueResponse) getResultForObject(GetIssueResponse.class, response);
        return JSONObject.parseObject(getIssueResponse.getData(), GetIssueResponse.Issue.class);
    }

    protected String getBaseUrl() {
        if (ENDPOINT.endsWith("/")) {
            return ENDPOINT.substring(0, ENDPOINT.length() - 1);
        }
        return ENDPOINT;
    }

    public void setConfig(ZentaoConfig config) {
        if (config == null) {
            MSException.throwException("config is null");
        }
        USER_NAME = config.getAccount();
        PASSWD = config.getPassword();
        ENDPOINT = config.getUrl();
    }
}
