package io.metersphere.track.issue.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.zentao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public abstract class ZentaoClient extends BaseClient {

    protected  String ENDPOINT;

    protected  String USER_NAME;

    protected  String PASSWD;

    public RequestUrl requestUrl;
    protected String url;

    public ZentaoClient(String url) {
        ENDPOINT = url;
    }

    // 注意 recTotal={1}&recPerPage={2}&pageID={3} 顺序不能调换，实在恶心
    private static final String BUG_LIST_URL="?m=bug&f=browse&productID={0}&branch=&browseType=&param=0&orderBy=&recTotal={1}&recPerPage={2}&pageID={3}&t=json&zentaosid={4}";

    public String login() {
        GetUserResponse getUserResponse = new GetUserResponse();
        String sessionId = "";
        try {
            sessionId = getSessionId();
            String loginUrl = requestUrl.getLogin();
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("account", USER_NAME);
            paramMap.add("password", PASSWD);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(loginUrl + sessionId, HttpMethod.POST, requestEntity, String.class);
            getUserResponse = (GetUserResponse) getResultForObject(GetUserResponse.class, response);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        GetUserResponse.User user = getUserResponse.getUser();
        if (user == null) {
            LogUtil.error(JSONObject.toJSON(getUserResponse));
            // 登录失败，获取的session无效，置空session
            MSException.throwException("zentao login fail, user null");
        }
        if (!StringUtils.equals(user.getAccount(), USER_NAME)) {
            LogUtil.error("login fail，inconsistent users");
            MSException.throwException("zentao login fail, inconsistent user");
        }
        return sessionId;
    }

    public String getSessionId() {
        String getSessionUrl = requestUrl.getSessionGet();
        ResponseEntity<String> response = restTemplate.exchange(getSessionUrl,
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
            String bugCreate = requestUrl.getBugCreate();
           response = restTemplate.exchange(bugCreate + sessionId,
                    HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        AddIssueResponse addIssueResponse = (AddIssueResponse) getResultForObject(AddIssueResponse.class, response);
        return JSONObject.parseObject(addIssueResponse.getData(), AddIssueResponse.Issue.class);
    }

    public void updateIssue(String id, MultiValueMap<String, Object> paramMap) {
        String sessionId = login();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.exchange(requestUrl.getBugUpdate(),
                    HttpMethod.POST, requestEntity, String.class, id, sessionId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    public void deleteIssue(String id) {
        String sessionId = login();
        try {
            restTemplate.exchange(requestUrl.getBugDelete(),
                    HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class, id, sessionId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    public JSONObject getBugById(String id) {
        String sessionId = login();
        String bugGet = requestUrl.getBugGet();
        ResponseEntity<String> response = restTemplate.exchange(bugGet,
                HttpMethod.GET, null, String.class, id, sessionId);
        GetIssueResponse getIssueResponse = (GetIssueResponse) getResultForObject(GetIssueResponse.class, response);
        return JSONObject.parseObject(getIssueResponse.getData());
    }

    public JSONArray getBugsByProjectId(String projectId, int pageNum, int pageSize) {
        String sessionId = login();
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + BUG_LIST_URL,
                HttpMethod.GET, null, String.class, projectId, 9999999, pageSize, pageNum, sessionId);
        return JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONArray("bugs");
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
