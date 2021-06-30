package io.metersphere.track.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.track.issue.domain.tapd.TapdConfig;
import io.metersphere.track.issue.domain.tapd.TapdGetIssueResponse;
import io.metersphere.track.issue.domain.tapd.TapdStatusMapResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
public class TapdClient extends BaseClient {

    protected  String ENDPOINT = "https://api.tapd.cn";

    protected  String USER_NAME;

    protected  String PASSWD;


    public TapdGetIssueResponse getIssueForPage(String projectId, int pageNum, int limit) {
       return getIssueForPageByIds(projectId, pageNum, limit, null);
    }

    public TapdStatusMapResponse getStatusMap(String projectId) {
        String url = getBaseUrl() + "/workflows/status_map?workspace_id={1}&system=bug";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class, projectId);
        return (TapdStatusMapResponse) getResultForObject(TapdStatusMapResponse.class, response);
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
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class,
                projectId, pageNum, limit, fields, idStr);
        return (TapdGetIssueResponse) getResultForObject(TapdGetIssueResponse.class, response);
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
