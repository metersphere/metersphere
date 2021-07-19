package io.metersphere.track.issue.client;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.tapd.AddTapdIssueResponse;
import io.metersphere.track.issue.domain.tapd.TapdBug;
import io.metersphere.track.issue.domain.tapd.TapdConfig;
import io.metersphere.track.issue.domain.tapd.TapdGetIssueResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Component
public class TapdClient extends BaseClient {

    protected  String ENDPOINT = "https://api.tapd.cn";

    protected  String USER_NAME;

    protected  String PASSWD;


    public TapdGetIssueResponse getIssueForPage(String projectId, int pageNum, int limit) {
       return getIssueForPageByIds(projectId, pageNum, limit, null);
    }

    public Map<String, String> getStatusMap(String projectId) {
        String url = getBaseUrl() + "/workflows/status_map?workspace_id={1}&system=bug";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class, projectId);
        String resultForObject = (String) getResultForObject(String.class, response);
        JSONObject jsonObject = JSONObject.parseObject(resultForObject);
        String data = jsonObject.getString("data");
        return JSONObject.parseObject(data, Map.class);
    }

    public TapdGetIssueResponse getIssueForPageByIds(String projectId, int pageNum, int limit, List<String> ids) {
        String url = getBaseUrl() + "/bugs?workspace_id={1}&page={2}&limit={3}&fields={4}";
        StringBuilder idStr = new StringBuilder();
        ids.forEach(item -> {
            idStr.append(item + ",");
        });
        if (!CollectionUtils.isEmpty(ids)) {
            url += "&id={5}";
        }
        String fields = "id,title,description,priority,severity,reporter,status";
        LogUtil.info("ids: " + idStr);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class,
                projectId, pageNum, limit, fields, idStr);
        return (TapdGetIssueResponse) getResultForObject(TapdGetIssueResponse.class, response);
    }

    public TapdBug addIssue(MultiValueMap<String, Object> paramMap) {
        String url = getBaseUrl() + "/bugs";
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, getAuthHeader());
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return ((AddTapdIssueResponse) getResultForObject(AddTapdIssueResponse.class, response)).getData().getBug();
    }

    protected HttpEntity<MultiValueMap> getAuthHttpEntity() {
        return new HttpEntity<>(getAuthHeader());
    }

    protected HttpHeaders getAuthHeader() {
        return getBasicHttpHeaders(USER_NAME, PASSWD);
    }

    protected String getBaseUrl() {
        return ENDPOINT;
    }

    public void setConfig(TapdConfig config) {
        if (config == null) {
            MSException.throwException("config is null");
        }
        USER_NAME = config.getAccount();
        PASSWD = config.getPassword();
    }
}
